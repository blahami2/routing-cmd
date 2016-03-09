/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing;

import cz.certicon.routing.model.Config;
import cz.certicon.routing.data.basic.FileSource;
import java.io.File;
import java.io.IOException;
import cz.certicon.routing.data.ConfigReader;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Route {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main( String[] args ) throws IOException {
        String configFilePath = System.getProperty("user.dir") + File.separator + "config.xml";
        if ( args.length > 0 ) {
            configFilePath = args[0];
        }
        File configFile = new File(configFilePath);
        if(!configFile.exists()){
            System.out.println( "File does not exist: " + configFilePath );
            return;
        }
        ConfigReader configIo;
        Config config = configIo.read( new FileSource(configFile));
    }
    
}
