# FlowSight

Small client-side Fabric mod for Minecraft 26.2: press **O** to see flowing water.

Every water block that is not a source gets a tinted box and an arrow showing where it flows:

- **cyan**: strong flow, right next to a source
- **orange**: weak flow, at the edge of the spread
- **violet**: falling water (arrow points down)
- source blocks stay unmarked

Works underwater, where flowing and source water normally look the same. Handy for draining
ocean monuments, building water elevators or finding the last source block in a pool.
The key can be changed under Options → Controls → FlowSight.

## Install

Needs [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api).
Download the jar from the latest [Actions run](https://github.com/JeyKul/FlowSight/actions)
(artifact `flowsight-jar`) and put it in your `mods` folder. Client only; servers don't need it.

## Build

```sh
./gradlew build   # needs JDK 25; jar in build/libs/
```

## License

MIT
