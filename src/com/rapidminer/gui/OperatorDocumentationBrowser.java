/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.Process;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.renderer.RendererService;
import com.rapidminer.gui.tools.ExtendedHTMLJEditorPane;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ResourceDockKey;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.XMLException;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

/**
 * 
 * This class contains methods that generate an item that shows a help text eiter from an xml-file if provided or from the description contained by the operator itself.
 * The actual document is generated by the class OperatorDocToHtmlConverter.
 * @author Philipp Kersting
 * 
 */
public class OperatorDocumentationBrowser extends JPanel implements Dockable, ProcessEditor  {
	
	//Global Elements
	final ExtendedHTMLJEditorPane editor = new ExtendedHTMLJEditorPane("text/html", "<html>-</html>");
	ExtendedJScrollPane scrollPane =  new ExtendedJScrollPane();

	/** Location of the documentation file relative to the users directory.
	 * 
	 *  TODO: As soon as documentation is complete, copy files to RapidMiner project and user resources. */
	public static final String DOCUMENTATION_ROOT="../RapidMinerReferenceManual_en/documents/core/";
	//public static  String ressourceDirectory="C:\\Users\\kersting\\workspace\\RapidMiner_Unuk\\resources\\com\\rapidminer\\resources\\icons\\24\\";
	//public static List<File> filesInDirectory= null;
	private static final long serialVersionUID = 1L;
	public static Operator displayedOperator = null;
	public static int linkCounter = 0;
	public File currentFile = null;
	private boolean ignoreSelections = false;
	/////////////////////////////////
	
	/*
	 * 
	 * Prepares the dockable and its visible elements to be visible.
	 * 
	 */
	public OperatorDocumentationBrowser() {
		setLayout(new BorderLayout());
		
		//Instantiate Editor snd set Settings

		editor.installDefaultStylesheet();
		editor.addHyperlinkListener(new LinkHandle());
		editor.setEditable(false);
		HTMLEditorKit hed = new HTMLEditorKit();
		hed.setStyleSheet(createStyleSheet());
		editor.setEditorKit(hed);
		
		//add editor to scrollPane
		
		scrollPane = new ExtendedJScrollPane(editor);
		
		scrollPane.setMinimumSize(new Dimension(100,100));
		scrollPane.setPreferredSize(new Dimension(100,100));


		//add scrollPane to Dockable
		
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setVisible(true);
		this.validate();
		
	}
	@Override
	public void processChanged(Process process) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	
	/**
	 * 
	 * This method gets called if the user clicks on an operator that has been placed in the process.
	 * 
	 */
	
	public void setSelection(List<Operator> selection) {
		if (!selection.get(0).equals(displayedOperator)&&!ignoreSelections){
			displayedOperator = selection.get(0);
			assignDocumentation();
		}
	}
	
	/**
	 * 
	 * This is called by the setSelection() method. It creates an absolute path that indicates the corresponding documentation xml-file.
	 * 
	 */
	private void assignDocumentation() {
		String groupPath = ((displayedOperator.getOperatorDescription().getGroup()).replace(".", "/"));
		File userHome = new File(System.getProperty("user.dir"));
		
		String operatorDescriptionXmlPath = DOCUMENTATION_ROOT + groupPath + "/" + displayedOperator.getOperatorDescription().getKey() + ".xml";
		File f = new File(userHome, operatorDescriptionXmlPath);
		changeDocumentation(operatorDescriptionXmlPath);
		currentFile = f;
	}
	

	/**
	 * 
	 * This method gets called if the user clicks on an operator in the Operator-Tree View.
	 * 
	 */
	
	public void setSelection(Operator selection) {

		if (!selection.equals(displayedOperator)&&!ignoreSelections){
			displayedOperator = selection;
			assignDocumentation();
		}
	}
	
	
	@Override
	public void processUpdated(Process process) {
		
	}
 
	@Override
	public Component getComponent() {
		return this;
	}

	public static final String OPERATOR_HELP_DOCK_KEY = "operator_documentation_browser";
	//private static final String stylesheet = "C:/users/kersting/workspace/RapidMinerReferenceManual_en/documentationview.xsl";
	private static final String STYLESHEET_RESOURCE = "/com/rapidminer/resources/documentationview.xslt";
	private final DockKey DOCK_KEY = new ResourceDockKey(OPERATOR_HELP_DOCK_KEY);
	
	@Override
	public DockKey getDockKey() {
		return DOCK_KEY;
	}

	/**
	 * 
	 * Returns a new instance of OperatorDocumentationBrowser.
	 * 
	 */
	public static OperatorDocumentationBrowser instantiate() {
		return new OperatorDocumentationBrowser();
	}

	/**
	 * 
	 * This is the method that actually gets the Content of this Dockable.
	 * The conversion takes place in the class OperatorDocToHtmlConverter.
	 * 
	 */
	
	public String parseXmlAndReturnHtml(String xmlFile){
		
		try {
			//System.out.println( OperatorDocToHtmlConverter.convert(xmlFile, xsltFile).getText().length());
			return OperatorDocToHtmlConverter.convert(xmlFile, STYLESHEET_RESOURCE, displayedOperator);
		} catch (MalformedURLException e) {
			LogService.getRoot().log(Level.WARNING, "Failed to load documentation: "+e.toString(), e);
			return I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.error.operator_documentation_error.message", e.getLocalizedMessage());
		} catch (IOException e) {
			LogService.getRoot().log(Level.WARNING, "Failed to load documentation: "+e.toString(), e);
			return I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.error.operator_documentation_error.message", e.getLocalizedMessage());
		} finally{
		}
	}	
	
	/**
	 * 
	 *In the following you'll find static methods that are used by the xslt-script that is responsible for the display of the xml-files.
	 *These return the paths of image paths for the operator and portkeys in return for the class' name as String.	 
	 *
	 */
	/**
	 * 
	 * Searches for a class with the given name and returns the path of the ressource.
	 * Used for the images of the ports' data types.
	 * 
	 * @param type - the class' name as String
	 * @return the path of the ressource of the corresponding icon.
	 */
	@SuppressWarnings("unchecked")
	public static String getIconNameForType(String type) {
		String iconName;
		if ((type == null) || type.isEmpty()) {
			iconName = "plug.png";					
		} else {
			Class<? extends IOObject> typeClass;
			try {
				typeClass = (Class<? extends IOObject>) Class.forName(type);
				
				//iconName = RendererService.getName(typeClass);
				
				iconName = RendererService.getIconName(typeClass);	
				if (iconName == null) {
					iconName = "plug.png";
				}
			} catch (ClassNotFoundException e) {
				LogService.getRoot().log(Level.WARNING, "Failed to lookup class "+type, e);
				iconName = "plug.png";
			}
		}
		
		String path = SwingTools.getIconPath("24/"+iconName);
		return path;
		
	}
	/**
	 * 
	 * Searches for a class with the given name and returns the path of the ressource.
	 * Used for the images of the ports' data types, but not by the xslt script, but by the method in OperatorDocToHtmlConverter that turns the local description properties into an html document.
	 * 
	 * @param clazz - the class as Class.
	 * @return the path of the ressource of the corresponding icon.
	 */
	public static String getIconNameForType(Class<? extends IOObject> clazz) {
		String iconName = "plug.png";
		String path = null;
		Class<? extends IOObject> typeClass;
		typeClass = clazz;
		iconName = RendererService.getIconName(typeClass);	
		if (iconName == null) {
			iconName = "plug.png";
		}
		try {
		path = SwingTools.getIconPath("24/"+iconName);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return path;
		
	}
	
	/**
	 * 
	 * Returns the name of a type in exchange for its class' name.
	 *  
	 * @param type - the class' name as String
	 * @return the short name of the class as String
	 */
	@SuppressWarnings("unchecked")
	public static String getTypeNameForType(String type) {
		String iconName;
		if ((type == null) || type.isEmpty()) {
			iconName = "";					
		} else {
			Class<? extends IOObject> typeClass;
			try {
				typeClass = (Class<? extends IOObject>) Class.forName(type);
				
				iconName = " ("+RendererService.getName(typeClass)+")";
			} catch (ClassNotFoundException e) {
				LogService.getRoot().log(Level.WARNING, "Failed to lookup class "+type, e);
				System.err.println("Type Icon: Failed lookup class");
				iconName = "";
			}
		}
		return iconName;
		
	}
	/**
	 * Returns the ressourcepath of the icon that belongs to the Opeator whose key was handed over.
	 * 
	 * @param operatorKey - The Key of the operator.
	 * @return - The path of the ressource that contains the icon of the operator.
	 */
	public static String getIconNameForOperator(String operatorKey) {
		if (displayedOperator == null) {
			return null;
		} else {
			String iconName; 
			iconName = SwingTools.getIconPath("24/"+displayedOperator.getOperatorDescription().getIconName());
			return iconName;
		}
		
	}

/**
 * 
 * Actually just returns a number that increases every time this method is called.
 * This number is needed for identifying the position of the clicked link of the selected example process.
 * The linkCounter will be reset every time a new operator is selected.
 * This is necessary since the for-each loops in xslt do not provide a counter and xslt-variables cannot be overridden.
 * Only determined fo the xslt-script and nothing else.
 *  
 * @return The number of the link.
 */
	public static int linkIncrement(){
		linkCounter++;
		return linkCounter;
	}
/** 
 * 
 * Returns whether the parameter of the currently selected operator responding to the given key is an expert parameter.
 * @param key - The key of the parameter
 * @return Whether the parameter is Expert.
 */
	public static String expert(String key){
		if (displayedOperator.getParameterType(key).isExpert()){
			return " (Expert)";
		}else{
			return null;
		}
		
	}
	/**
	 * 
	 * A little event handler that handles the clicking on a link of a tutorial process.
	 * 
	 * @author kersting
	 *
	 */
	class LinkHandle implements HyperlinkListener{
		public void	hyperlinkUpdate(HyperlinkEvent e)
		{
			if (e.getEventType().toString()=="ACTIVATED")
			{
				// TODO: implement function for turoial process here
				if (e.getDescription().contains("l")){
					int index =Integer.parseInt( e.getDescription().substring(1));
					Operator recent = displayedOperator;
					ignoreSelections = true;
					RapidMinerGUI.getMainFrame().setProcess(displayedOperator.getOperatorDescription().getOperatorDocumentation().getExamples().get(index).getProcess(), true);
					Collection<Operator> displayedOperators = RapidMinerGUI.getMainFrame().getProcess().getAllOperators();
					for (Operator item : displayedOperators){
						if (item.getClass().equals(recent.getClass())){
							RapidMinerGUI.getMainFrame().selectOperator(item);
							ignoreSelections = false;
						}
					}
					
				}else{
					try{
						Document document = XMLTools.parse(currentFile);
						
						NodeList nodeList = document.getElementsByTagName("tutorialProcess");
						Node processNode = nodeList.item(Integer.parseInt(e.getDescription())-1);
						Node process = null;
						int i = 0;
						while (i<processNode.getChildNodes().getLength()){
							if (processNode.getChildNodes().item(i).getNodeName().equals("process")){
								process = processNode.getChildNodes().item(i);
							}
							i++;
						}
						
						StringWriter buffer = new StringWriter();
						DOMSource processSource = new DOMSource(process);
						Transformer t = TransformerFactory.newInstance().newTransformer();
						t.transform(processSource, new StreamResult(buffer));
						Process exampleProcess = new Process(buffer.toString());
						Operator formerOperator = displayedOperator;
						ignoreSelections = true;
						RapidMinerGUI.getMainFrame().setProcess(exampleProcess, true);
						Collection<Operator> displayedOperators = RapidMinerGUI.getMainFrame().getProcess().getAllOperators();
						for (Operator item : displayedOperators){
							if (item.getClass().equals(formerOperator.getClass())){
								RapidMinerGUI.getMainFrame().selectOperator(item);
								ignoreSelections = false;
							}
						}
						System.err.println("INFO: Loading example process No. "+e.getDescription() +" of Operator \"" + formerOperator.getName() +"\"");
					} catch (TransformerException te) {
						System.err.println("TransformerException occured");
						LogService.getRoot().log(Level.WARNING, "Example process couldn't be created :" +te.getStackTrace(), te);
					}catch (SAXException se) {
						System.err.println("SAXException occured");
						LogService.getRoot().log(Level.WARNING, "Error while parsing xml! Example process couldn't be created :" +se.getStackTrace(), se);
					} catch (IOException ioe) {
						LogService.getRoot().log(Level.WARNING, "Error while reading file! Example process couldn't be created :" +ioe.toString(), ioe);
					} catch (XMLException xe) {
						System.err.println("XMLException occured");
						LogService.getRoot().log(Level.WARNING, "Error while parsing xml! Example process couldn't be created :" +xe.getStackTrace(), xe);
					}
				}
			}
		}
	}

	/**
	 * 
	 * A method that handles the renewance of the documentation text.
	 * 
	 * @param xmlFilename - Just passes the ressource of the destination xml-File a little further.
	 */
	public void changeDocumentation(String xmlFilename){
		linkCounter = 0;
		String html = parseXmlAndReturnHtml(xmlFilename);
		html = html.replace(" xmlns:rmdoc=\"com.rapidminer.gui.OperatorDocumentationBrowser\"", " ");
		editor.setContentType("text/html");
		editor.setText("<html>"+html+"</html>");	
		editor.setCaretPosition(0);
	}
	
	/**
	 * 
	 * This method creates and returns a stylesheet that makes the documentation look as it's supposed to look.
	 * 
	 * @return The stylesheet.
	 */
	public StyleSheet createStyleSheet(){
		StyleSheet css = new HTMLEditorKit().getStyleSheet();
		css.addRule("* {font-family: Arial}");
		css.addRule("h4 {margin-bottom:2px; margin-top:2ex; padding-left:4px; padding:0; color:#446699; font-size:16pt}");
		css.addRule("p {padding: 0px 20px 1px 20px; font-family: Arial;}");			
		css.addRule("ul li {padding-bottom:1ex}");
		css.addRule("hr {color:red; background-color:red}");
		css.addRule("h3 {color: #3399FF}");
		css.addRule(".typeIcon {height: 10px; width: 10px;}");
		css.addRule("td {vertical-align: top}");
		css.addRule(".lilIcon {padding: 2px 4px 2px 0px}");
		css.addRule(".HeadIcon {height: 40px; width: 40px}");
		css.addRule("td {font-style: normal}");
		
		return css;
		
	}

}
