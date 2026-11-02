# Boot Flasher

Boot Flasher is an Android application for flashing boot images directly to the device boot partition using root privileges.

## Features

- Root access detection.
- Boot partition detection.
- Partition device path and size information.
- Android document picker for `.img` files.
- Image size validation.
- Flash confirmation dialog.
- Direct image writing using `dd`.
- Dark user interface.
- Adaptive Android application icon.

## Requirements

- Android 8.0 (API 26) or newer.
- Root access through Magisk, KernelSU or a compatible root solution.
- A device with an accessible boot partition.
- Android SDK 36 for building.

## Build

Requirements:

- JDK 17
- Android SDK
- Gradle Wrapper included in the repository.

Clone the repository:

```bash
git clone https://github.com/USERNAME/BootFlasher.git
cd BootFlasher
