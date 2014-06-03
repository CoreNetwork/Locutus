package com.mcnsa.chat.plugin.managers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mcnsa.chat.plugin.annotations.ComponentInfo;
import com.mcnsa.chat.plugin.annotations.DatabaseTableInfo;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
//TODO Haven't even looked at, need close examination
public class ComponentManager {
	public class Component {
		public Class<?> clazz = null;
		public Object instance = null;
		public boolean disabled = false;
		public ArrayList<String> disabledCommands = new ArrayList<String>();
		public ComponentInfo componentInfo = null;
		public LinkedList<CommandManager.CommandInfo> commands = new LinkedList<CommandManager.CommandInfo>();
	}
	
	private HashMap<String, Component> registeredComponents = new HashMap<String, Component>();
	
	public ComponentManager() {
		// load all the classes in our desired package (the components package)
		try {
			// get the code source that we're in
			CodeSource src = CommandManager.class.getProtectionDomain().getCodeSource();
			if(src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				
				// get our class loader
				File myFile = new File("plugins/MCNSAChat.jar");
				URL myJarFileURL = new URL("jar", "", "file:" + myFile.getAbsolutePath() + "!/");
				URL[] classes = {myJarFileURL};
				URLClassLoader classLoader = new URLClassLoader(classes, this.getClass().getClassLoader());
				
				// now loop over our files
				ZipEntry ze = null;
				assert(classLoader != null);
				while((ze = zip.getNextEntry()) != null) {
					String entryName = ze.getName();
					if(entryName.endsWith(".class") && entryName.startsWith("com/mcnsa/chat/plugin/components/")) {						
						// get it's class
						Class<?> clazz = Class.forName(entryName.replaceAll("/", ".").substring(0, entryName.length() - 6), true, classLoader);
						
						// get its information
						if(!clazz.isAnnotationPresent(ComponentInfo.class)) {
							// no component info!
							// skip it!
							//Logger.debug("no component info for class '%s'! Skipping...", clazz.getSimpleName());
							continue;
						}
						ComponentInfo ci = clazz.getAnnotation(ComponentInfo.class); 
						
						// create a component object
						Component component = new Component();
						component.clazz = clazz;
						component.instance = null; // don't initialize yet!
						component.componentInfo = ci;
						
						// register an instance of it
						registeredComponents.put(clazz.getSimpleName().toLowerCase(), component);
						// if it does, add it to the list of tables to be created
						if(clazz.isAnnotationPresent(DatabaseTableInfo.class)) {
							// get our info
							DatabaseTableInfo tableInfo = clazz.getAnnotation(DatabaseTableInfo.class);
							
							// register it with the database manager
							DatabaseManager.addTableConstruct(tableInfo);
						}
					}
				}
			}
			else {
				ConsoleLogging.warning("code source was null!");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			ConsoleLogging.warning("Failed to load component classes!");
		}
	}
	
	public void initializeComponents() {
		// initialize all non-disabled components
		for(String component: registeredComponents.keySet()) {
			if(!registeredComponents.get(component).disabled) {
				try {
					registeredComponents.get(component).instance = registeredComponents.get(component).clazz.newInstance();
				}
				catch(Exception e) {
					ConsoleLogging.warning("Failed to instantiate component "+component+" : "+e.getMessage());
				}
			}
		}
	}
	
	public HashMap<String, Component> getRegisteredComponents() {
		return registeredComponents;
	}
}
