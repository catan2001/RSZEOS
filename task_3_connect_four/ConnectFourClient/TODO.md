# TODO
- [x] Basic Server
- [x] Basic Client
- [x] After the client connects to the server, the server should send and OK signal and then the client should send it's username so that the server knows who it is. After this the server should send all of the active ready to play clients to the client.
- [x] Make client side array list of all active players clickable/selectable
- [x] Create a Python Test app in tools/tcp_client.py
- [x] What if Server closes? Regulate this by restoring the app data
- [x] client reject-accept
- [x] client-client handshake
- [x] game GUI
- [x] game logic

- [ ] BUG: there is option for a user to send multiple times request and start a game...
- [ ] BUG: double removal of the player when the player enters a game and then exits app...
- [ ] BUG: when one user exits the app while the game is ongoing the other player stays in the game...
- [ ] MISSING: no draw situation is handled currently
- [ ] BUG: When creating MainActivity there is no changing of the handle side
- [ ] BUG: There can be two same usernames...
