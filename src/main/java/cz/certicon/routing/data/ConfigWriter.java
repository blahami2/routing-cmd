/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import cz.certicon.routing.model.Config;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ConfigWriter {
    
    public ConfigWriter open() throws IOException;
    public ConfigWriter write(Config config) throws IOException;
    public ConfigWriter close() throws IOException;
    
}
