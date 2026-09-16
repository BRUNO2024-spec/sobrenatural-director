# Oracle Cloud ingress — manual step

Open Oracle Cloud Console → **Networking** → **Virtual Cloud Networks** → the
VCN of the VPS → the instance subnet → **Security List** or attached **NSG** →
**Add Ingress Rule**.

Use:

- Source CIDR: `0.0.0.0/0` (prefer tester IP `/32` ranges when possible)
- IP protocol: `TCP`
- Destination port: `25565`
- Description: `Minecraft Director Shadow Research`

Do not select all protocols or all ports. Do not change SSH. OpenCode has no
Oracle control-plane access, so this remains `USER_ACTION_REQUIRED` until the
administrator confirms it in the console.
