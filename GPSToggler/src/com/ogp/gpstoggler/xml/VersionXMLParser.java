package com.ogp.gpstoggler.xml;

import java.io.InputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import android.content.ContextWrapper;
import android.util.Log;
import android.util.Xml;


public class VersionXMLParser 
{
	static private final String TAG		= "VersionXMLParser";
	
	static private Boolean versionPrinted = false;
	
	
	static public class XMLEngine
	{
		public XMLEngine (ContextWrapper context)
		{
			DefaultHandler contentHandle = new DefaultHandler()
			{
				public void startElement (String 		uri, 
										  String 		localName, 
										  String 		qName, 
										  Attributes	attributes)
				{
					if (localName.equalsIgnoreCase ("version"))
					{
						String version = attributes.getValue ("version");
						if (version.length() > 0)
						{
							versionPrinted = true;
												
							Log.i(TAG, String.format ("Remote engine version: %s", 
												  	  version));
							
							com.ogp.gpstoggler.StateMachine.setVersion (version);
						}
					}
					else
					{
						Log.e(TAG, String.format ("XMLEngine(). Error: unknow 'open' clause [%s].",
												  localName));
					}
				}

				public void endElement (String 		uri, 
										String 		localName, 
										String 		qName)
				{
					if (localName.equalsIgnoreCase ("version"))
					{
					}
					else
					{
						Log.e(TAG, String.format ("XMLEngine(). Error: unknow 'close' clause [%s].",
												  localName));
					}
				}
			};

			Log.i(TAG, "XMLEngine(). Entry...\n");


			try
			{
				InputStream stream = context.getAssets().open ("version.xml");

				Xml.parse (stream, 
						   Xml.Encoding.UTF_8, 
						   contentHandle);

				if (!versionPrinted)
				{
					Log.w(TAG, "XMLEngine(). Failed to get version and build number.");
				}
				
			}
			catch(Exception e)
			{
				Log.e(TAG, "XMLEngine(). List XML processing failed.");
			}
		}
	}

	
	
	static public void printVersion (ContextWrapper 	context)
	{
		synchronized(versionPrinted)
		{
			if (!versionPrinted)
			{
				new XMLEngine(context);
			}
		}
	}
}
