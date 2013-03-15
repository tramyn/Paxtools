package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Provides a simple editor map for a level with a given factory.
 *
 * This class initializes 3 singletons( 1 for each level) from a tab delimited text resources that lists the
 * properties and their domains. This is done to remove any dependencies to Jena.
 *
 * The recommended usage is to use the {@link #get(org.biopax.paxtools.model.BioPAXLevel)} method.
 *
 * @author Emek Demir
 */
public enum SimpleEditorMap implements EditorMap
{

	L1(BioPAXLevel.L1),
	L2(BioPAXLevel.L2),
	L3(BioPAXLevel.L3);

	private static final Log log = LogFactory.getLog(EditorMapAdapter.class);

	private final SimpleEditorMapImpl impl;


	SimpleEditorMap(BioPAXLevel level)
	{
		this.impl = new SimpleEditorMapImpl(level);
	}

	/**
	 * To obtain a copy of the editor map for the corresponding level, use the
	 * @param level
	 * @return
	 */
	public static SimpleEditorMap get(BioPAXLevel level)
	{
		for (SimpleEditorMap value : values())
		{
			if (value.getLevel().equals(level)) return value;
		}
		//should never reach here
		throw new IllegalBioPAXArgumentException("Unknown level:" + level);
	}

	// fixed: should be 'static' member class, to save memory, because it does not need to link and access SimpleEditorMap instance fields
	static class SimpleEditorMapImpl extends EditorMapAdapter implements EditorMap
	{
		private final BioPAXLevel level;

		private List<ObjectPropertyEditor> sorted; //TODO use it somewhere or remove!

		SimpleEditorMapImpl(BioPAXLevel level)
		{
			this.level = level;
			InputStream stream = this.getClass().getResourceAsStream(level + "Editor.properties");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			try
			{
				readEditors(level, reader);
			}
			catch (IOException e)
			{
				log.error("Could not initialize " + "Editor Map", e);
			}
			finally
			{
				try
				{
					stream.close();
				}
				catch (IOException ignore)
				{
					log.error("Could not close stream! Exiting");
					System.exit(1);
				}
			}

		}



		private void readEditors(BioPAXLevel level, BufferedReader reader) throws IOException
		{
			String line = reader.readLine();
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreElements())
			{
				this.registerModelClass(st.nextToken());
			}

			while ((line = reader.readLine()) != null)
			{
				st = new StringTokenizer(line);
				String domain = st.nextToken();
				Class<? extends BioPAXElement> domainInterface = this.getLevel().getInterfaceForName(domain);

				String propertyName = st.nextToken();
				Map<Class<? extends BioPAXElement>,Set<Class<? extends BioPAXElement>>> rangeRestrictions =
						new HashMap<Class<? extends BioPAXElement>, Set<Class<? extends BioPAXElement>>>();
				while (st.hasMoreTokens())
				{
					String rToken = st.nextToken();
					if (rToken.startsWith("R:"))
					{
						StringTokenizer rt = new StringTokenizer(rToken.substring(2), "=");
						Class<? extends BioPAXElement> rDomain = level.getInterfaceForName(rt.nextToken());
						Set<Class<? extends BioPAXElement>> rRanges =
								new HashSet<Class<? extends BioPAXElement>>();
						for (StringTokenizer dt = new StringTokenizer(rt.nextToken(), ","); dt.hasMoreTokens();)
						{
							rRanges.add(level.getInterfaceForName(dt.nextToken()));
						}
						rangeRestrictions.put(rDomain,rRanges);
					}

				}



				createAndRegisterBeanEditor(propertyName, domainInterface,rangeRestrictions);
			}
		}

		public BioPAXLevel getLevel()
		{
			return level;
		}
	}


	public <D extends BioPAXElement> PropertyEditor<? super D, ?> getEditorForProperty(String property,
	                                                                                   Class<D> javaClass)

	{
		return impl.getEditorForProperty(property, javaClass);
	}

	@Override public Set<PropertyEditor> getEditorsForProperty(String property)
	{
		return impl.getEditorsForProperty(property);
	}

	@Override public <D extends BioPAXElement> Set<PropertyEditor<? extends D, ?>> getSubclassEditorsForProperty(
			String property, Class<D> domain)
	{
		return impl.getSubclassEditorsForProperty(property, domain);
	}

	@Override public Set<PropertyEditor> getEditorsOf(BioPAXElement bpe)
	{
		return impl.getEditorsOf(bpe);

	}

	@Override public Set<ObjectPropertyEditor> getInverseEditorsOf(BioPAXElement bpe)
	{
		return impl.getInverseEditorsOf(bpe);
	}

	@Override public <E extends BioPAXElement> Set<Class<E>> getKnownSubClassesOf(Class<E> javaClass)
	{
		return impl.getKnownSubClassesOf(javaClass);
	}


	public BioPAXLevel getLevel()
	{
		return impl.getLevel();

	}

	@Override public Set<PropertyEditor> getEditorsOf(Class<? extends BioPAXElement> domain)
	{
		return impl.getEditorsOf(domain);
	}

	@Override public Set<ObjectPropertyEditor> getInverseEditorsOf(Class<? extends BioPAXElement> domain)
	{
		return impl.getInverseEditorsOf(domain);
	}

    @Override
    public Iterator<PropertyEditor> iterator() {
        return impl.iterator();
    }


}
