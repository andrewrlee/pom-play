package uk.co.optimisticpanda.pom;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public enum TemplateManager {
	;
	
	public static void print(final Map<String, Object> model) {
		Configuration cfg = new Configuration();
		try (Writer out = new OutputStreamWriter(System.out)) {
			Template template = loadTemplate(cfg);
			template.process(model, out);
			out.flush();
		} catch (TemplateException | IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private static Template loadTemplate(Configuration cfg) {
		try {
			StringTemplateLoader loader = new StringTemplateLoader();
			loader.putTemplate("template.ftl", Resources.toString(getResource("template.ftl"), UTF_8));
			cfg.setTemplateLoader(loader);
			return cfg.getTemplate("template.ftl");
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
