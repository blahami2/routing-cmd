/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ResultIoFactory {

    public ResultWriter createResultWriter( DataDestination dataDestination );

    public ResultReader createResultReader( DataSource dataSource );
}
