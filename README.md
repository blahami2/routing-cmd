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

## Versions ##

### 1.2 ###
- Switched to directed graphs
- Graph file creation is not recommended (please, provide valid graph files in folder named NAME.pbf based on the pbf file provided - TO BE FIXED)

### 1.1 ###
- Added new content to the config file: path to the reference route statistics
- Provided statistics are compared to the result
- The data are exported into the output directory
- Please, update your config file or let the application generate a new one as well as a template for the reference config file
