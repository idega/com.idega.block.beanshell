package com.idega.block.beanshell.business;


public interface BSHEngine extends com.idega.business.IBOService
{
 public bsh.Interpreter getBSHInterpreter() throws java.rmi.RemoteException;
 public java.lang.String getBshVersion() throws java.rmi.RemoteException;
 public java.lang.Object runScript(java.lang.String scriptString,com.idega.presentation.IWContext p1)throws bsh.EvalError,bsh.TargetError, java.rmi.RemoteException;
 public java.lang.Object runScript(java.lang.String scriptString)throws bsh.EvalError, java.rmi.RemoteException;
 public java.lang.Object runScriptFromBundle(com.idega.idegaweb.IWBundle p0,java.lang.String scriptFile,com.idega.presentation.IWContext p2)throws java.io.FileNotFoundException,bsh.EvalError, java.rmi.RemoteException;
 public java.lang.Object runScriptFromBundle(com.idega.idegaweb.IWBundle p0,java.lang.String scriptFile)throws java.io.FileNotFoundException,bsh.EvalError, java.rmi.RemoteException;
 public java.lang.Object runScriptFromFileWithPath(java.lang.String path)throws java.io.FileNotFoundException,bsh.EvalError, java.rmi.RemoteException;
 public java.lang.Object runScriptFromFileWithPath(java.lang.String path,com.idega.presentation.IWContext p1)throws java.io.FileNotFoundException,bsh.EvalError, java.rmi.RemoteException;
 public java.lang.Object runScriptFromURL(String URL)throws java.io.FileNotFoundException,bsh.EvalError, java.rmi.RemoteException;
 public String doExecuteScript(String script) throws Exception;
}
