/*
 * Created on 14.2.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.idega.block.beanshell.presentation;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;

import bsh.EvalError;
import bsh.TargetError;

import com.idega.block.beanshell.business.BSHEngine;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.util.expression.ELUtil;

/**
 * WORK IN PROGRESS, see todo list in eclipse
 * This Block is both an editor for a <a href="http://www.beanshell.org">Beanshell</a> script and a bsh script runner.<br>
 * Add the BeanShellScript to a page and it will run the script (if one is set) when its main method is called.<br>
 * You will have all the request parameters available to you in your script and the current instance of IWContext.<br>
 * An example usage might be to handle Form validation, adding presentation object to a page if certain parameters are in the request, debugging code, live code testing etc.<br>
 * The possibilities are endless!<br>
 * Object you can use in your script without initializing them are: iwc (IWContext), any request parameter by their name (e.g. ib_page)<br>
 * Examples: <br>
 * 1. "print(iwc.getParameter("X"));" - Would print to the stdout the value of the request parameter X <br>
 * 2. "return new com.idega.block.news.presentation.NewsReader()" - Would add a News block to the page where the script was added <br>
 * 3. "import com.idega.presentation.*; <br>Table table = new Table(10,10);<br>table.setColor("red");<br>return  table;" - Adds a red table to the page where the script was added <br>
 * 
 * For now a script from the live editor can only be run as the super admin for security reasons. This will be a role later on...
 * 
 * See <a href="http://www.beanshell.org">www.beanshell.org</a> for a tutorial in the Beanshell scripting language and some examples.<br>
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 0.8
 */
public class BeanShellScript extends Block {


	private static final String IW_BUNDLE_IDENTIFIER="com.idega.block.beanshell";
	private boolean showEditor = false;
	private String scriptString;
	private static final String PARAM_SCRIPT_STRING = "bsh_scr_str";
	private String scriptURL;
	private IWBundle bundle;
	private String scriptInBundleFileName;
	private String fileNameWithPath;
	private List parametersToMaintain;
	
	
	public BeanShellScript() {
		super();
	}
	
	
	@Override
	public void main(IWContext iwc) throws RemoteException{
		//TODO Eiki make safe to execute from parameter
		//TODO Eiki allow multiple scripts per page and support ordering scripts
		//TODO Eiki Put editor in a window
		BSHEngine engine;
		try {
			engine = (BSHEngine) IBOLookup.getServiceInstance(iwc,BSHEngine.class);
		
			if(scriptString==null && iwc.isSuperAdmin()){
				scriptString = iwc.getParameter(PARAM_SCRIPT_STRING);
			}
			
			if(showEditor){
				addEditorAndRunScript(iwc,engine);
			}
			else{
				Object obj = runScript(iwc,engine);
				if(obj!=null){
					addResultObject(null, obj);
				}
			}
		
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
	}
	

	private Object runScript(IWContext iwc, BSHEngine engine) throws RemoteException {
		Object obj = null;
		
		try{
			if(scriptString!=null){
				//run script from scriptstring
				obj = engine.runScript(scriptString,iwc);
			}
			else if(getScriptInBundleFileName()!=null){
				//run from a file within a bundle
				if(bundle==null){
					bundle = this.getBundle(iwc);
				}
				
				obj = engine.runScriptFromBundle(bundle,scriptInBundleFileName);
			}
			else if(getScriptURL()!=null){
				//run from a script file in the db
				obj = engine.runScriptFromURL(getScriptURL());
			}
			else if(getFileNameWithPath()!=null){
				//run from a script file from anywhere on the server
				obj = engine.runScriptFromFileWithPath(getFileNameWithPath());
			}
		}
		catch (TargetError e) {
			System.err.println("[IW BeanShellScript] - The script or code called by the script threw an exception: " + e.getTarget());
			obj = new Text("The script or code called by the script threw an exception: " + e.getTarget());
			e.printStackTrace();
		}
		catch (EvalError e2) {
			System.err.println("[IW BeanShellScript] - There was an error in evaluating the script:" + e2);
			obj = new Text("There was an error in evaluating the script:" + e2);
			e2.printStackTrace();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			
			if(scriptInBundleFileName!=null){
				obj = new Text("Script file was not found: "+bundle.getRealPathWithFileNameString(scriptInBundleFileName));
			}
			else if(fileNameWithPath!=null){
				obj = new Text("Script file was not found: "+fileNameWithPath);
			}
		}
		
		return obj;
	}


	@SuppressWarnings("cast")
	private void addEditorAndRunScript(IWContext iwc, BSHEngine engine) throws RemoteException {
		
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
		this.getParentPage().addJavascriptURL(web2.getCodePressScriptFilePath());
		
		Form editorForm = new Form();
		
		if(parametersToMaintain!=null && !parametersToMaintain.isEmpty()){
			editorForm.maintainParameters(parametersToMaintain);
		}
		
		Table table = new Table(1,3);
		
		TextArea scriptArea = new TextArea(PARAM_SCRIPT_STRING,( (scriptString!=null)? scriptString : ""));
		scriptArea.setStyleClass("codepress java linenumbers-on");
		
		
		scriptArea.setWidth("640");
		scriptArea.setHeight("480");
		
		table.add(scriptArea ,1,2);
		table.add(new SubmitButton(),1,3);
		
		Object obj = runScript(iwc,engine);
		addResultObject(table, obj);
		
		editorForm.add(table);
		add(editorForm);
	}


	private void addResultObject(Table table, Object obj) {
		if(obj!=null){
			if(table!=null){
				if(obj instanceof PresentationObject){
					table.add((PresentationObject)obj,1,1);
				}
				else if(obj instanceof UIComponent){
					table.add((UIComponent)obj,1,1);
				}
				else{
					table.add(obj.toString(),1,1);
				}
			}
			else{
				if(obj instanceof PresentationObject){
					add((PresentationObject)obj);
				}
				else if(obj instanceof UIComponent){
					add((UIComponent)obj);
				}
				else{
					add(obj.toString());
				}
			}
		}
	}


	@Override
	public String getBundleIdentifier(){
		return IW_BUNDLE_IDENTIFIER;
	}
	
	public void setToShowScriptEditor(boolean showEditor){
		this.showEditor = showEditor;
	}
	
	public boolean isScriptEditorVisible(){
		return showEditor;
	}
	
	public void setScriptString(String scriptString){
		this.scriptString = scriptString;
	}
	
	public String getScriptString(){
		return scriptString;
	}
	
	public void setScriptURL(String scriptURL){
		this.scriptURL = scriptURL;
	}
	
	public void setScriptFileNameWithPath(String fileNameWithPath){
		this.fileNameWithPath = fileNameWithPath;
	}
	
	public void setBundleAndScriptFileName(IWBundle bundle, String fileName){
		scriptInBundleFileName = fileName;
		this.bundle = bundle;
	}
	
	public void setScriptFileNameAndUseDefaultBundle(String fileName){
		setBundleAndScriptFileName(null,fileName);
	}


	/**
	 * Adds the parameter name to a list of parameters to maintain over requests.
	 * @param parameter
	 */
	public void addParameterToMaintain(String parameter) {
		if(parametersToMaintain==null){
			parametersToMaintain = new ArrayList();
		}
		parametersToMaintain.add(parameter);
	}


	public IWBundle getBundle() {
		return bundle;
	}


	public void setBundle(IWBundle bundle) {
		this.bundle = bundle;
	}


	public String getFileNameWithPath() {
		return fileNameWithPath;
	}


	public void setFileNameWithPath(String fileNameWithPath) {
		this.fileNameWithPath = fileNameWithPath;
	}


	public List getParametersToMaintain() {
		return parametersToMaintain;
	}


	public void setParametersToMaintain(List parametersToMaintain) {
		this.parametersToMaintain = parametersToMaintain;
	}


	public String getScriptInBundleFileName() {
		return scriptInBundleFileName;
	}


	public void setScriptInBundleFileName(String scriptInBundleFileName) {
		this.scriptInBundleFileName = scriptInBundleFileName;
	}


	public String getScriptURL() {
		return scriptURL;
	}


}
