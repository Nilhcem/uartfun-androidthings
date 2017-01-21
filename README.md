# Android Things UART fun

A simple project demonstrating what you can do with Android Things, a Rainbow HAT, and a USB-TTL cable.

## Demo

See this tweet for a demo video:
[https://twitter.com/Nilhcem/status/822827197262168065](https://twitter.com/Nilhcem/status/822827197262168065)


## Required stuff

- Raspberry Pi 3 Model B
- UART cable (e.g. PL2303HX)
- Rainbow HAT


## Setup

- Connect the Rainbow HAT on the Raspberry Pi, the UART cable TX wire to the Rainbow HAT RX pin, and the UART cable RX wire to the Rainbow HAT TX pin
- By default the UART port is mapped to the linux console, disable this behaviour mounting the Raspberry Pi micro sd *(wherein Android Things is installed)* on your host computer, then edit `/path/to/mnt/sdcard/cmdline.txt` to remove the following kernel boot argument:
```
console=serial0,115200
```
- Put back the sd card into the Raspberry Pi, turn it on, deploy the application
- Plug the UART cable into a host PC, start your favorite terminal program (e.g. screen, cutecom), and connect to the USB-TTL port at 115200 baud.
```
$ screen /dev/ttyUSB0 115200
```
- Now you can start sending commands from your host keyboard


![photo][]

## Commands

* `Rainbow HAT A button`: Text mode *(pressing keyboard keys to display some text)*
* `Rainbow HAT B button`: Toggle mode *(if in text mode, enables music mode and vice versa)*
* `Rainbow HAT C button`: Music mode *(pressing keyboard keys to play some music)*
* `Keyboard F key`: Play note: Fa# (F#)
* `Keyboard G key`: Play note: Sol# (G#)
* `Keyboard H key`: Play note: La (A)
* `Keyboard J key`: Play note: Si (B)
* `Keyboard K key`: Play note: Do# (C#)
* `Keyboard L key`: Play note: Re (D)
* `Keyboard Space key`: Stop current note


## Thanks

These links made me save some precious time:

[UART loopback sample](https://github.com/androidthings/sample-uartloopback)

[UART peripherals on Android Things for Raspberry Pi 3](http://stackoverflow.com/questions/41127018/uart-peripherals-on-android-things-for-raspberry-pi-3)


## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[photo]: https://raw.githubusercontent.com/Nilhcem/uartfun-androidthings/master/rainbowhat.jpg
