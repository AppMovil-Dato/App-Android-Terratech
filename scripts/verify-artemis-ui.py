#!/usr/bin/env python3
"""Deterministic black-box Android UI journey using the real Artemis controller/helper.

Run only through verify-backend-journey.py with TB1_E2E_DRIVER=artemis.
No LLM, API key, app internals, Compose test nodes, or direct HTTP writes are used.
"""

import asyncio
import base64
import datetime
import io
import json
import logging
import os
import pathlib
import re
import subprocess
import time
import uuid
import xml.etree.ElementTree as ET
from PIL import Image
from artemis.mcp.adb_server import _get_controller

ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / os.environ.get("TB1_EVIDENCE_DIR", "docs/evidence/artemis")
OUT.mkdir(parents=True, exist_ok=True)
SERIAL = os.environ.get("TB1_ANDROID_SERIAL", "emulator-5554")
if not SERIAL.startswith("emulator-") or os.environ.get("TB1_E2E_DRIVER") != "artemis":
    raise SystemExit("Run this through the isolated Artemis emulator harness.")
STRINGS = {
    x.attrib["name"]: x.text
    for x in ET.parse(ROOT / "app/src/main/res/values/strings.xml").getroot()
}
PACKAGE = "com.novatech.terratech"
ADB = os.environ.get(
    "TB1_ADB", str(pathlib.Path.home() / "Library/Android/sdk/platform-tools/adb")
)
for logger in logging.Logger.manager.loggerDict.values():
    if isinstance(logger, logging.Logger):
        logger.setLevel(logging.WARNING)


class Journey:
    def __init__(self):
        self.controller = _get_controller(SERIAL)
        self.steps = []
        self.last = None

    async def screen(self):
        self.last = await asyncio.to_thread(
            self.controller.ctx.ui_adb_client.get_screen_data
        )
        return [
            e
            for e in self.last.elements
            if e.get("package") == PACKAGE and e.get("visible-to-user") != "false"
        ]

    @staticmethod
    def center(e):
        b = e["parsed_bounds"]
        return ((b["left"] + b["right"]) // 2, (b["top"] + b["bottom"]) // 2)

    async def node(self, label, timeout=20, scroll=False, last=False, clickable=False):
        deadline = time.monotonic() + timeout
        swipes = 0
        while time.monotonic() < deadline:
            rows = await self.screen()
            found = [
                e
                for e in rows
                if (e.get("text") == label or e.get("content-desc") == label)
                and 130 <= self.center(e)[1] <= self.last.height - 100
            ]
            if clickable:

                def actionable(e):
                    b = e["parsed_bounds"]
                    return e.get("clickable") == "true" or any(
                        n.get("clickable") == "true"
                        and n.get("enabled") == "true"
                        and n["parsed_bounds"]["left"] <= b["left"]
                        and n["parsed_bounds"]["top"] <= b["top"]
                        and n["parsed_bounds"]["right"] >= b["right"]
                        and n["parsed_bounds"]["bottom"] >= b["bottom"]
                        for n in rows
                    )

                found = [e for e in found if actionable(e)]
            if found:
                return found[-1 if last else 0]
            if scroll and swipes < 10:
                containers = [e for e in rows if e.get("scrollable") == "true"]
                if containers:
                    b = max(
                        containers,
                        key=lambda e: (
                            e["parsed_bounds"]["bottom"] - e["parsed_bounds"]["top"]
                        ),
                    )["parsed_bounds"]
                    x = (b["left"] + b["right"]) // 2
                    error = await self.controller.swipe_coords(
                        x,
                        int(b["top"] + (b["bottom"] - b["top"]) * 0.78),
                        x,
                        int(b["top"] + (b["bottom"] - b["top"]) * 0.25),
                        350,
                    )
                    if error:
                        raise AssertionError(error)
                    swipes += 1
            await asyncio.sleep(0.35)
        raise AssertionError(f"UI did not expose expected label: {label}")

    async def expect(self, key, timeout=20, scroll=False):
        label = STRINGS.get(key, key)
        await self.node(label, timeout=timeout, scroll=scroll)
        self.steps.append({"assert": key, "passed": True})
        print("PASS", key, flush=True)

    async def click(self, key, scroll=True, last=False):
        e = await self.node(
            STRINGS.get(key, key),
            scroll=scroll,
            last=last,
            clickable=key not in ("home", "fields", "profile"),
        )
        result = await self.controller.tap_at(*self.center(e))
        if result.error:
            raise AssertionError(result.error)
        self.steps.append({"action": "tap", "target": key})
        await asyncio.sleep(0.3)

    async def hide_keyboard(self):

        state = await self.controller.controller.execute_shell("dumpsys input_method")
        if "mInputShown=true" in state:
            await self.controller.press_key("4")
            deadline = time.monotonic() + 5
            while time.monotonic() < deadline:
                state = await self.controller.controller.execute_shell(
                    "dumpsys input_method"
                )
                if "mInputShown=true" not in state:
                    await asyncio.sleep(0.6)
                    return
                await asyncio.sleep(0.2)
            raise AssertionError("Keyboard did not close")

    async def input(self, key, value):
        await self.hide_keyboard()

        for _ in range(10):
            e = await self.node(STRINGS[key], scroll=True)
            rows = await self.screen()
            x, y = self.center(e)
            edits = [
                n
                for n in rows
                if n.get("class") == "android.widget.EditText"
                and n["parsed_bounds"]["left"] <= x <= n["parsed_bounds"]["right"]
                and n["parsed_bounds"]["top"] - 30
                <= y
                <= n["parsed_bounds"]["bottom"] + 30
            ]
            if not edits:
                raise AssertionError(f"No editable field associated with label {key}")
            if self.center(edits[0])[1] < self.last.height * 0.5:
                break
            containers = [n for n in rows if n.get("scrollable") == "true"]
            if not containers:
                break
            b = max(
                containers,
                key=lambda n: n["parsed_bounds"]["bottom"] - n["parsed_bounds"]["top"],
            )["parsed_bounds"]
            error = await self.controller.swipe_coords(
                x,
                int(b["top"] + (b["bottom"] - b["top"]) * 0.75),
                x,
                int(b["top"] + (b["bottom"] - b["top"]) * 0.35),
                350,
            )
            if error:
                raise AssertionError(error)
            await asyncio.sleep(0.3)

        await asyncio.sleep(0.5)
        e = await self.node(STRINGS[key], scroll=True)
        result = await self.controller.tap_at(*self.center(e))
        if result.error:
            raise AssertionError(result.error)
        deadline = time.monotonic() + 8
        while time.monotonic() < deadline:
            observed = await self.screen()
            labels = [n for n in observed if n.get("text") == STRINGS[key]]
            if labels:
                lx, ly = self.center(labels[0])
                if any(
                    n.get("focused") == "true"
                    and n.get("class") == "android.widget.EditText"
                    and n["parsed_bounds"]["left"] <= lx <= n["parsed_bounds"]["right"]
                    and n["parsed_bounds"]["top"] - 30
                    <= ly
                    <= n["parsed_bounds"]["bottom"] + 30
                    for n in observed
                ):
                    break
            await asyncio.sleep(0.2)
        else:
            raise AssertionError(f"Field did not gain focus: {key}")
        client = self.controller.ctx.ui_adb_client

        if not client.clear_text():
            raise AssertionError(f"Clear failed: {key}")
        deadline = time.monotonic() + 8
        while time.monotonic() < deadline:
            rows = await self.screen()
            focused = [
                n
                for n in rows
                if n.get("class") == "android.widget.EditText"
                and n.get("focused") == "true"
            ]
            if focused and not focused[0].get("text"):
                break
            await asyncio.sleep(0.15)
        else:
            raise AssertionError(f"Field did not clear: {key}")
        for attempt in range(2):
            if attempt and not client.clear_text():
                raise AssertionError(f"Retry clear failed: {key}")
            if not client.send_text(value):
                raise AssertionError(f"Input failed: {key}")
            deadline = time.monotonic() + 20
            accepted = False
            while time.monotonic() < deadline:
                rows = await self.screen()
                focused = [
                    n
                    for n in rows
                    if n.get("class") == "android.widget.EditText"
                    and n.get("focused") == "true"
                ]
                if focused and (
                    focused[0].get("text") == value
                    or (
                        focused[0].get("password") == "true"
                        and len(focused[0].get("text", "")) == len(value)
                    )
                ):
                    accepted = True
                    break
                await asyncio.sleep(0.15)
            if accepted:
                break
        else:
            raise AssertionError(f"Field did not accept expected text: {key}")
        await self.hide_keyboard()
        self.steps.append({"action": "input", "target": key, "valueRecorded": False})

    async def capture(self, name):
        await self.screen()
        Image.open(io.BytesIO(base64.b64decode(self.last.base64))).save(
            OUT / f"{name}.png"
        )

    def radio(self, enabled):
        for kind in ["wifi", "data"]:
            subprocess.run(
                [
                    ADB,
                    "-s",
                    SERIAL,
                    "shell",
                    "svc",
                    kind,
                    "enable" if enabled else "disable",
                ],
                check=True,
            )

    async def logout(self):
        await self.click("profile")
        await self.click("logout")
        await self.expect("logout_title")
        await self.click("logout", last=True)
        await self.expect("welcome")

    async def run(self):
        email = f"artemis-{uuid.uuid4()}@test.example"
        password = f"Tb1-{uuid.uuid4()}"
        assert await self.controller.launch_app(PACKAGE)
        await self.expect("welcome")
        await self.capture("login")
        await self.click("no_account")
        for key, value in [
            ("full_name", "Ana Torres"),
            ("email", email),
            ("password", password),
            ("confirmation", "wrong-confirmation"),
        ]:
            await self.input(key, value)
        await self.click("register")
        await self.expect("error_confirmation")
        await self.input("confirmation", password)
        await self.click("register")
        await self.expect("new_field")
        assert not any(e.get("text") == STRINGS["welcome"] for e in await self.screen())
        self.steps.append(
            {"assert": "signupEntersAppWithoutOnboarding", "passed": True}
        )
        await self.capture("registered-home")
        await self.logout()
        await self.click("no_account")
        for key, value in [
            ("full_name", "Ana Torres"),
            ("email", email),
            ("password", password),
            ("confirmation", password),
        ]:
            await self.input(key, value)
        await self.click("register")
        await self.expect("error_duplicate")
        await self.click("have_account")
        await self.input("email", email)
        await self.input("password", "wrong-password")
        await self.click("login", last=True)
        await self.expect("error_credentials")
        await self.input("password", password)
        await self.click("login", last=True)
        await self.expect("new_field")
        await self.click("new_field")
        await self.input("phone", "999888777")
        await self.click("continue_action")
        for key, value in [
            ("farm_name", "Fundo Artemis"),
            ("location", "Huaral, Lima"),
            ("area_ha", "1.25"),
        ]:
            await self.input(key, value)
        await self.click("profile_finish")
        await self.expect("profile_saved")
        await self.capture("profile")
        await self.click("profile_continue_fields")
        await self.input("field_name", "Parcela Norte")
        await self.input("crop", "Papa")
        await self.click("continue_action")
        await self.click("map_draw")

        deadline = time.monotonic() + 30
        while time.monotonic() < deadline:
            rows = await self.screen()
            maps = [e for e in rows if e.get("content-desc") == STRINGS["field_map"]]
            if maps:
                break
            await asyncio.sleep(0.3)
        else:
            raise AssertionError("Google Maps surface was not exposed")
        b = max(
            maps, key=lambda e: e["parsed_bounds"]["bottom"] - e["parsed_bounds"]["top"]
        )["parsed_bounds"]
        if b["bottom"] >= self.last.height - 190:
            await self.controller.swipe_coords(
                self.last.width // 2,
                int(self.last.height * 0.72),
                self.last.width // 2,
                int(self.last.height * 0.4),
                350,
            )
            maps = [
                e
                for e in await self.screen()
                if e.get("content-desc") == STRINGS["field_map"]
            ]
            b = max(
                maps,
                key=lambda e: e["parsed_bounds"]["bottom"] - e["parsed_bounds"]["top"],
            )["parsed_bounds"]
        await asyncio.sleep(3)
        for x, y in [(0.25, 0.25), (0.70, 0.25), (0.70, 0.65), (0.25, 0.65)]:
            result = await self.controller.tap_at(
                int(b["left"] + (b["right"] - b["left"]) * x),
                int(b["top"] + (b["bottom"] - b["top"]) * y),
            )
            assert not result.error, result.error
            self.steps.append({"action": "mapTap", "passed": True})
            await asyncio.sleep(0.5)
        await self.expect(STRINGS["map_points"].replace("%1$d", "4").replace("%d", "4"))
        await self.capture("map-polygon")
        await self.click("continue_action")
        await self.capture("field-review")
        await self.click("create_field")
        await self.expect("Parcela Norte")
        await self.capture("fields")
        await self.click("associate")
        await self.input("sensor_code", "BAD")
        await self.input("sensor_name", "Sensor Norte")
        await self.click("associate")
        await self.expect("error_sensor")
        await self.input("sensor_code", "TT-000000")
        await self.click("associate")
        await self.expect("error_sensor")
        await self.input("sensor_code", "TT-ZZZ001")
        await self.click("associate")
        await self.expect("view_history", scroll=True)
        await self.click("back")
        await self.expect("view_sensor")
        await self.click("associate")
        await self.input("sensor_code", "TT-ZZZ001")
        await self.input("sensor_name", "Repeated sensor")
        await self.click("associate")
        await self.expect("error_occupied")
        await self.click("back")
        await self.click("view_sensor")
        await self.expect("SIMULATED", 60)
        await self.capture("sensor")
        await self.click("view_history")
        await self.expect("days_7")
        await self.click("days_30")
        await self.expect("Readings: 720", 45)
        await self.capture("history")
        deadline = time.monotonic() + 20
        while time.monotonic() < deadline:
            rows = await self.screen()
            values = [
                e for e in rows if re.fullmatch(r"\d+(?:[.,]\d+)? %", e.get("text", ""))
            ]
            if values:
                break
            await self.controller.swipe_coords(
                self.last.width // 2,
                int(self.last.height * 0.75),
                self.last.width // 2,
                int(self.last.height * 0.3),
                350,
            )
        else:
            raise AssertionError("No measured humidity in history list")
        result = await self.controller.tap_at(*self.center(values[0]))
        assert not result.error, result.error
        await self.expect("reading_detail")
        await self.expect("SIMULATED")
        await self.capture("reading-detail")
        self.radio(False)
        await self.click("home")
        await self.click("retry")
        await self.expect("offline", 35)
        await self.expect("SIMULATED")
        await self.capture("offline")
        assert await self.controller.terminate_app(PACKAGE)
        assert await self.controller.launch_app(PACKAGE)
        await self.expect("offline", 35)
        await self.expect("Parcela Norte")
        await self.expect("SIMULATED")
        await self.capture("offline-recreated")
        self.radio(True)
        await self.logout()
        await self.click("no_account")
        for key, value in [
            ("full_name", "Bruno Rojas"),
            ("email", f"artemis-{uuid.uuid4()}@test.example"),
            ("password", password),
            ("confirmation", password),
        ]:
            await self.input(key, value)
        await self.click("register")
        await self.expect("new_field")
        await self.click("fields")
        await self.expect("no_fields")
        assert not any(e.get("text") == "Parcela Norte" for e in await self.screen())
        self.steps.append(
            {"assert": "secondAccountCannotSeeFirstAccountField", "passed": True}
        )
        await self.capture("second-account")
        await self.logout()
        await self.input("email", email)
        await self.input("password", password)
        await self.click("login", last=True)
        await self.expect("SIMULATED", 40)
        await self.click("view_history")
        await self.click("days_30")
        await self.expect("Readings: 720", 45)
        await self.click("home")
        await self.capture("home")


async def main():
    j = Journey()
    error = None
    try:
        await j.run()
    except Exception as exc:
        error = str(exc)
        await j.capture("failure")
        raise
    finally:
        j.radio(True)
        if error is None:
            (OUT / "failure.png").unlink(missing_ok=True)
        (OUT / "ui-results.json").write_text(
            json.dumps(
                {
                    "engine": "Artemis UnifiedMobileController + Accessibility Helper",
                    "llmUsed": False,
                    "deviceSerial": SERIAL,
                    "passed": error is None,
                    "error": error,
                    "assertionsPassed": sum(
                        x.get("passed", False) for x in j.steps if "assert" in x
                    ),
                    "checksPassed": sum(x.get("passed", False) for x in j.steps),
                    "steps": j.steps,
                    "timestampUtc": datetime.datetime.now(
                        datetime.timezone.utc
                    ).isoformat(),
                },
                indent=2,
            )
            + "\n"
        )
        await j.controller.cleanup()


if __name__ == "__main__":
    asyncio.run(main())
