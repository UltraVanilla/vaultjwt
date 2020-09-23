VaultJwt
===

Plugin for Spigot servers that cryptographically signs the role and account UUID a player has, and displays a link with that token in the form of a JSON Web Token.

Useful for when you need to connect forum/IRC/Discord accounts to a Minecraft server account with a secure, self-serve system

Setup
---

Requires Vault API.

Run:

```bash
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -pkeyopt ec_param_enc:named_curve -out vaultjwt.key
openssl ec -in jwt.key -pubout -out vaultjwt.key.pub
```

Paste `vaultjwt.key` into the `privKey` section of `plugins/VaultJwt/config.yml`, replacing the included example key. **Do not expose this key publicly, as it allows anyone to identify themselves as anyone to your service.**

Give a copy of `vaultjwt.key.pub` to the third party service verifying a signature (e.g. your discord/irc bot, your website, etc.)

Configure `authTokenUrl` with the self-serve URL you wish to direct users to when they run `/token`

Spigot commands & permissions
---

`vaultjwt.token` `/token` - Generate a token for the player running it. It is safe to give this permission to everyone. Only the console can specify an argument, the username to generate a token for.

`vaultjwt.bulktoken` `/bulktoken` - Generate a token for everyone who has ever run the command. These tokens do not allow authenticating as users, so it's safe (but pointless) to give this permission to everyone. This is designed for when you wish to update everyone's roles at once.

JSON format
---

When `/token` is run, a JWT with the following format is produced

```javascript
{
    exp: 1600829926,  // expires in 1 hour
    uuid: "2318e8d5-4b49-44b8-8a68-a7921fdf00a6",
    groups: ["admin", "moderator", "grandchampion"]
}
```

When `/bulktoken` is run, a JWT with the following format is produced

```javascript
{
    exp: 1600829926,
    players: [{
        disallowAuth: true,  // indicating this token does not allow one to identify as this player
        uuid: "48c3d9ce-19b7-4bb0-b3d2-d0260f0976b3",
        groups: ["admin", "moderator", "grandchampion"]
    }, {
        disallowAuth: true,
        uuid: "48c3d9ce-19b7-4bb0-b3d2-d0260f0976b3",
        groups: ["elder"]
    }]
}
```

Compile from source
---

```bash
mvn install
```

License
---

This source code is dual-licensed under choice of CC0 or MIT. See `LICENSE` and `LICENSE-MIT`

**Contributing**

By making a contribution to this repository, I certify that:

- (a) The contribution was created in whole or in part by me and I have the right to submit it under the open source license indicated in the file; or

- (b) The contribution is based upon previous work that, to the best of my knowledge, is covered under an appropriate open source license and I have the right under that license to submit that work with modifications, whether created in whole or in part by me, under the same open source license (unless I am permitted to submit under a different license), as indicated in the file; or

- (c) The contribution was provided directly to me by some other person who certified (a), (b) or (c) and I have not modified it.

- I understand and agree that this project and the contribution are public and that a record of the contribution (including all personal information I submit with it, including my sign-off) is maintained indefinitely and may be redistributed consistent with this project or the open source license(s) involved.
