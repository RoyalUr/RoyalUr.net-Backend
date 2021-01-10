# 🎲 The Royal Game of Ur
A website for playing the ancient Mesopatamian board game, the Royal Game of Ur! 

<p align="center"><a href="https://royalur.net">
  <img src="https://github.com/Sothatsit/RoyalUrClient/blob/master/banner.jpg?raw=true">
</a></p>

This repository holds the server code for The Royal Game of Ur, https://royalur.net.
The client code of The Royal Game of Ur can be found in the
[RoyalUrClient repository](https://github.com/Sothatsit/RoyalUrClient).

The Royal Game of Ur is based upon an ancient Sumerian board game in the British Museum.
The original board game can be dated to 2600 BC, and was discovered in a royal tomb in
the city-state of Ur in ancient Mesopotamia.  Learn more about the game on
[Wikipedia](https://en.wikipedia.org/wiki/Royal_Game_of_Ur), or watch a fun match
between Tom Scott and Irving Finkel on [YouTube](https://youtu.be/WZskjLq040I)!


# 🖥️ Compilation
This project uses Maven, and so can be compiled using the command, `mvn install`.

This will produce a jar file containing the servers
dependencies in the `/target/` directory.


# ⚙️ Configuration
The RoyalUrServer uses a JSON config file for its settings.
If you wish to use a config file, the `ROYAL_UR_SERVER_CONFIG`
environment variable should be set with a file location in which
to generate and read the config file from.


# 📝 License
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
