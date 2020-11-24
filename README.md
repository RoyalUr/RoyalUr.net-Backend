[![The Royal Game of Ur Logo](https://github.com/Sothatsit/RoyalUrClient/blob/master/logo.png?raw=true)](https://royalur.net)
------------------

This repository holds the server code for The Royal Game of Ur, https://royalur.net.

The client code of The Royal Game of Ur can be found in the
[RoyalUrClient repository](https://github.com/Sothatsit/RoyalUrClient).


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
