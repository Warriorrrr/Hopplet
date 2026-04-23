# Hopplet
Hopplet is a Paper plugin designed to allow more simplified and expressive item filtering than vanilla allows.

It features an easy to learn filtering language designed for the sole purpose of filtering items based on their properties.

Filters are edited through an intuitive user interface where you will receive immediate feedback on whether your filter will work or not.

You can learn how to use Hopplet at the wiki [here](https://github.com/jwkerr/Hopplet/wiki). You can also ask for help with designing filters in our [Discord](https://discord.gg/ey6ZvnwAJp).

## Syntax Examples
```
Access control: allow items thrown by tuzzzie to pass, or a book that is a copy of a copy and written by Fruitloopins.
thrower(tuzzzie) | (author(Fruitloopins) & generation(copy_of_copy))

Tag filtering: only allow items with the specified vanilla item and block tags to pass.
tag(wool, wool_carpets)

Material filtering: only allow items of the specified material to pass.
type(oak_wood, dark_oak_wood)

Display name filtering: only allow items that contain "stone" in their display name to pass.
name_contains(stone)

Exact display name filtering: only allow items with exactly the specified name to pass.
name("Super Secret Access Key")
```

## Credits
Thanks to LiveOverflow for the inspiration for this plugin in [this](https://youtu.be/Gi2PPBCEHuM?t=224) video.
