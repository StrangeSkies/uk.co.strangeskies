/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2bnd.
 *
 * uk.co.strangeskies.p2bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.bnd;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;

/**
 * This class is not primarily intended to be used within OSGi environments. For
 * an OSGi enabled implementation of {@link RemoteRepositoryPlugin} and
 * {@link Repository} which provides p2 repository support, the
 * {@code uk.co.strangeskies.p2.P2RepositoryImpl} class in the
 * {@code uk.co.strangeskies.p2} project should be used instead. This class is
 * simply a wrapper for that implementation for use in non OSGi environments,
 * and creates a framework internally to host the necessary Eclipse Project
 * bundles.
 * <p>
 *
 * @author Elias N Vasylenko
 */
public class P2BndRepository implements RemoteRepositoryPlugin, Repository, Plugin {
	private static final int FRAMEWORK_TIMEOUT_MILLISECONDS = 2000;
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 1500;

	private static final String OSGI_CLEAN = "osgi.clean";
	private static final String OSGI_CLEAR_PERSISTED_STATE = "clearPersistedState";
	private static final String OSGI_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";

	@SuppressWarnings("serial")
	static final Map<String, String> FRAMEWORK_PROPERTIES = new HashMap<String, String>() {
		{
			put(OSGI_CLEAN, Boolean.toString(true));
			put(OSGI_CLEAR_PERSISTED_STATE, Boolean.toString(true));
			put(OSGI_SYSTEM_PACKAGES_EXTRA,
					"uk.co.strangeskies.utilities;version=\"1.0.0\"," + "uk.co.strangeskies.p2;version=\"1.0.0\","
							+ "aQute.bnd.service;version=\"4.1.0\"," + "aQute.bnd.version;version=\"1.3.0\","
							+ "aQute.service.reporter;version=\"1.0.0\"," + "org.osgi.service.repository;version=\"1.0.0\"");
		}
	};

	private FrameworkWrapper frameworkWrapper;

	private Log log = (l, s) -> {
		System.out.println(l + ": " + s);
	};

	private P2Repository repositoryService;

	private Map<String, String> properties;
	private Reporter reporter;

	public static void main(String... args) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", "TestName");
		map.put("location", "http://download.eclipse.org/releases/mars/");

		P2BndRepository repo = new P2BndRepository();
		repo.initialise();
		repo.log = (l, s) -> System.out.println(l + ": " + s);
		repo.setProperties(map);

		System.out.println(repo.getName());
	}

	/**
	 * Create an unconfigured P2 repository accessible through Bnd.
	 */
	public P2BndRepository() {
		log.log(Level.INFO, "Creating P2BndRepository");
	}

	private void initialise() {
		try {
			Manifest manifest = ManifestUtilities.getManifest(getClass());

			log.log(Level.INFO, "Setting framework URLS");
			Set<URL> frameworkUrls = new HashSet<>();
			try {
				frameworkUrls.add(new URL(
						"file:///home/eli/workspaces/uk.co.strangeskies/uk.co.strangeskies.p2bnd/org.eclipse.osgi.jar"));
				frameworkUrls.add(new URL(
						"file:///home/eli/workspaces/uk.co.strangeskies/cnf/release/uk.co.strangeskies.osgi.frameworkwrapper.provider/uk.co.strangeskies.osgi.frameworkwrapper.provider-1.0.13.jar"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			log.log(Level.INFO, "Creating delegating classloader");
			ClassLoader classLoader = new URLClassLoader(frameworkUrls.toArray(new URL[frameworkUrls.size()]),
					FrameworkWrapper.class.getClassLoader());

			log.log(Level.INFO, "Fetching framework wrapper service loader");
			ServiceLoader<FrameworkWrapper> serviceLoader = ServiceLoader.load(FrameworkWrapper.class, classLoader);

			log.log(Level.INFO, "Loading framework wrapper service");
			frameworkWrapper = StreamSupport.stream(serviceLoader.spliterator(), false).findAny()
					.<RuntimeException>orElseThrow(
							() -> new RuntimeException("Cannot find service implementing " + FrameworkWrapper.class.getName()));

			log.log(Level.INFO, "Initialise framework wrapper properties");
			frameworkWrapper.setLog(getLog());

			frameworkWrapper.setTimeoutMilliseconds(FRAMEWORK_TIMEOUT_MILLISECONDS);

			frameworkWrapper.setLaunchProperties(FRAMEWORK_PROPERTIES);

			String frameworkJars = manifest.getMainAttributes().getValue(FrameworkWrapper.EMBEDDED_RUNPATH);
			frameworkWrapper.setBundles(Arrays.stream(frameworkJars.split(",")).map(s -> "/" + s.trim())
					.collect(Collectors.toMap(s -> "classpath:" + s, s -> getClass().getResourceAsStream(s))));

			frameworkWrapper.setInitialisationAction(() -> {
				frameworkWrapper.withServiceThrowing(P2Repository.class, p -> {
					repositoryService = p;
					if (properties != null)
						repositoryService.setProperties(properties);
					if (reporter != null)
						repositoryService.setReporter(reporter);
				}, SERVICE_TIMEOUT_MILLISECONDS);
			});
		} catch (Throwable e) {
			log.log(Level.ERROR, "Could not initialise P2BndRepository", e);
			throw e;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		frameworkWrapper.stopFramework();
		super.finalize();
	}

	@Override
	public void setProperties(Map<String, String> map) throws Exception {
		log.log(Level.WARN, "Setting P2 repository properties");
		synchronized (frameworkWrapper) {
			properties = map;
			if (frameworkWrapper.isStarted()) {
				repositoryService.setProperties(properties);
			}
		}
	}

	@Override
	public void setReporter(Reporter processor) {
		reporter = processor;

		log = new ReporterLog(reporter);

		log.log(Level.WARN, "Setting P2 repository reporter");

		initialise();
	}

	@Override
	public synchronized PutResult put(InputStream stream, PutOptions options) throws Exception {
		return frameworkWrapper.withFramework(() -> repositoryService.put(stream, options));
	}

	@Override
	public synchronized File get(String bsn, Version version, Map<String, String> properties,
			DownloadListener... listeners) throws Exception {
		return frameworkWrapper.withFramework(() -> repositoryService.get(bsn, version, properties, listeners));
	}

	@Override
	public boolean canWrite() {
		return frameworkWrapper.withFramework(() -> repositoryService.canWrite());
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return frameworkWrapper.withFramework(() -> repositoryService.list(pattern));
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		return frameworkWrapper.withFramework(() -> repositoryService.versions(bsn));
	}

	@Override
	public String getName() {
		return frameworkWrapper.withFramework(() -> repositoryService.getName());
	}

	@Override
	public String getLocation() {
		return frameworkWrapper.withFramework(() -> repositoryService.getLocation());
	}

	private Log getLog() {
		WeakReference<P2BndRepository> logSource = new WeakReference<>(this);

		return new Log() {
			@Override
			public void log(Level level, String message) {
				P2BndRepository repository = logSource.get();
				if (repository != null) {
					repository.log.log(level, message);
				}
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				P2BndRepository repository = logSource.get();
				if (repository != null) {
					repository.log.log(level, message);
				}
			}
		};
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		return frameworkWrapper.withFramework(() -> repositoryService.findProviders(requirements));
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		return frameworkWrapper.withFramework(() -> repositoryService.getHandle(bsn, version, strategy, properties));
	}

	@Override
	public synchronized File getCacheDirectory() {
		return frameworkWrapper.withFramework(() -> repositoryService.getCacheDirectory());
	}
}