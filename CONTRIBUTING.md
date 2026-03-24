# Contributing

## Build

```bash
mvn clean verify
```

Run the demo:

```bash
mvn -pl agc-demo-app -am spring-boot:run
```

## Pull requests

- Keep changes focused on a single concern.
- Run **`mvn verify`** before opening a PR (includes ArchUnit rules in `agc-architecture-tests`).
- Follow existing module boundaries: **`agc-core`** stays Spring-free; auto-configuration belongs in **`agc-spring-boot-autoconfigure`**; **`McpToolExecutor`** remains in **`com.framework.agent.mcp.internal`**.

## License

By contributing, you agree that your contributions are licensed under the same terms as the project ([LICENSE](LICENSE), Apache License 2.0).
