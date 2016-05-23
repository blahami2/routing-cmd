/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.xml;

import javax.xml.namespace.QName;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface XPathConstants<T> {

    public QName getXPathConstant();

    /*
    javax.xml.xpath.XPathConstants.BOOLEAN;
    javax.xml.xpath.XPathConstants.NODE;
    javax.xml.xpath.XPathConstants.NODESET;
    javax.xml.xpath.XPathConstants.NUMBER;
    javax.xml.xpath.XPathConstants.STRING;
    javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL;
     */
    public static final XPathConstants<Boolean> BOOLEAN = () -> javax.xml.xpath.XPathConstants.BOOLEAN;

    public static final XPathConstants<Node> NODE = () -> javax.xml.xpath.XPathConstants.NODE;
    public static final XPathConstants<NodeList> NODESET = () -> javax.xml.xpath.XPathConstants.NODESET;
    public static final XPathConstants<Double> NUMBER = () -> javax.xml.xpath.XPathConstants.NUMBER;
    public static final XPathConstants<String> STRING = () -> javax.xml.xpath.XPathConstants.STRING;
}
