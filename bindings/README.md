# Binding sources

`rg.apebind.yaml` in the repository root is the reviewed low-level CLI
contract. APEBind generates process execution code from that contract.

The handwritten semantic APIs live outside the generated files:

- Python: `bindings/python/src/rg_ape/search.py`
- Node.js: `bindings/node/src/search.js`
- Java: `bindings/java/src/main/java/rg_ape/SearchClient.java`

The semantic layer owns named-versus-raw calls, no-match handling, and public
API convenience. Generated code owns argument encoding and APE process execution.

## Regeneration

Run `./scripts/regenerate.sh` with `apebind` on `PATH`, or set
`APEBIND_BIN` to its executable. The script validates the contract and generates
into a temporary directory. It copies **only** generated source/runtime files
into `bindings/`. It does not replace public exports, semantic classes, package
metadata, or language-specific READMEs.

Each binding's `prepare.sh` still copies the released `rg.com` into the
package. Keep package versions and release assets in sync when publishing.
