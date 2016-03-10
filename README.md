# Router

## Requirements ##

- jre 1.8+
- PBF file with OSM maps (must have suffix .pbf)
- access to basic file system operations 

## Usage ##

    java -jar route-1.0-SNAPSHOT-jar-with-dependencies.jar [path/to/config]

- The 'config.xml' file is expected to be in the execution directory. See example 'config.xml' for more details.
- The template for 'config.xml' can be automatically created, when the application does not find one.

## Output ##

- File 'result.xml' in the [map_file_name]_result directory
    - list of nodes in the path (ordered)
    - list of edges in the path (ordered)
    - list of coordinates in the path (ordered)