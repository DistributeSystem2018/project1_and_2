package activitystreamer;


import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;

/**
 * UILogAppender
 * <p>
 *
 * retrieved form https://stackoverflow.com/questions/24005748/how-to-output-logs-to-a-jtextarea-using-log4j2
 */

@Plugin(name = "UILogAppender", category = "Core", elementType = "appender", printObject = true)
public class UILogAppender extends AbstractAppender {

	private static volatile JTextArea textArea = null;



	protected UILogAppender(String name, Layout<?> layout, Filter filter, boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}

	@PluginFactory
	public static UILogAppender createAppender(@PluginAttribute("name") String name,
												   @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
												   @PluginElement("Layout") Layout<?> layout,
												   @PluginElement("Filters") Filter filter) {

		if (name == null) {
			LOGGER.error("No name provided for JTextAreaAppender");
			return null;
		}

		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new UILogAppender(name, layout, filter, ignoreExceptions);
	}

	// Add the target JTextArea to be populated and updated by the logging information.
	public static void setTextArea(final JTextArea textArea) {
		UILogAppender.textArea = textArea;
	}

	@Override
	public void append(LogEvent event) {
		final String message = new String(this.getLayout().toByteArray(event));
		// Append formatted message to text area using the Thread.
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

						try {
							if (textArea != null) {
								if (textArea.getText().length() == 0) {
									textArea.setText(message);
								} else {
									textArea.append("\n" + message);
								}
							}
						} catch (final Throwable t) {
							System.out.println("Unable to append log to text area: "
									+ t.getMessage());
						}

				}
			});
		} catch (final IllegalStateException e) {
			// ignore case when the platform hasn't yet been iniitialized
		}
	}
}
