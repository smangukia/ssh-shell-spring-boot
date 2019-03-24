package com.github.fonimus.ssh.shell;

import com.github.fonimus.ssh.shell.auth.SshAuthentication;
import com.github.fonimus.ssh.shell.interactive.InteractiveInput;
import lombok.extern.slf4j.Slf4j;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.AbstractPosixTerminal;
import org.jline.utils.*;

import java.util.Arrays;
import java.util.List;

/**
 * Ssh shell helper for user interactions and authorities check
 */
@Slf4j
public class SshShellHelper {

    public static final List<String> DEFAULT_CONFIRM_WORDS = Arrays.asList("y", "yes");

    private final List<String> confirmWords;

    public SshShellHelper() {
        this(null);
    }

    public SshShellHelper(List<String> confirmWords) {
        this.confirmWords = confirmWords != null ? confirmWords : DEFAULT_CONFIRM_WORDS;
    }

    /**
     * Color message with given color
     *
     * @param message message to return
     * @param color   color to print
     * @return colored message
     */
    public String getColored(String message, PromptColor color) {
        return new AttributedStringBuilder().append(message, AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle())).toAnsi();
    }

    /**
     * Color message with given background color
     *
     * @param message         message to return
     * @param backgroundColor background color to print
     * @return colored message
     */
    public String getBackgroundColored(String message, PromptColor backgroundColor) {
        return new AttributedStringBuilder().append(message, AttributedStyle.DEFAULT.background(backgroundColor.toJlineAttributedStyle())).toAnsi();
    }

    /**
     * @param message      confirmation message
     * @param confirmWords (optional) confirmation words, default are {@link SshShellHelper#DEFAULT_CONFIRM_WORDS}, or configured in {@link SshShellProperties}
     * @return whether it has been confirmed
     */
    public boolean confirm(String message, String... confirmWords) {
        return confirm(message, false, confirmWords);
    }

    /**
     * @param message       confirmation message
     * @param caseSensitive should be case sensitive or not
     * @param confirmWords  (optional) confirmation words, default are {@link SshShellHelper#DEFAULT_CONFIRM_WORDS}, or configured in {@link SshShellProperties}
     * @return whether it has been confirmed
     */
    public boolean confirm(String message, boolean caseSensitive, String... confirmWords) {
        String response = read(message);
        List<String> confirm = this.confirmWords;
        if (confirmWords != null && confirmWords.length > 0) {
            confirm = Arrays.asList(confirmWords);
        }
        for (String c : confirm) {
            if (caseSensitive && c.equals(response)) {
                return true;
            } else if (!caseSensitive && c.equalsIgnoreCase(response)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Read from terminal
     *
     * @return response read from terminal
     */
    public String read() {
        return read(null);
    }

    /**
     * Print confirmation message and get response
     *
     * @param message message to print
     * @return response read from terminal
     */
    public String read(String message) {
        LineReader lr = SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getLineReader();
        if (message != null) {
            lr.getTerminal().writer().println(message);
        }
        lr.readLine();
        if (lr.getTerminal() instanceof AbstractPosixTerminal) {
            lr.getTerminal().writer().println();
        }
        return lr.getParsedLine().line();
    }

    /**
     * Color message with color {@link PromptColor#GREEN}
     *
     * @param message message to return
     * @return colored message
     */
    public String getSuccess(String message) {
        return getColored(message, PromptColor.GREEN);
    }

    /**
     * Color message with color {@link PromptColor#CYAN}
     *
     * @param message message to return
     * @return colored message
     */
    public String getInfo(String message) {
        return getColored(message, PromptColor.CYAN);
    }

    /**
     * Color message with color {@link PromptColor#YELLOW}
     *
     * @param message message to return
     * @return colored message
     */
    public String getWarning(String message) {
        return getColored(message, PromptColor.YELLOW);
    }

    /**
     * Color message with color {@link PromptColor#RED}
     *
     * @param message message to return
     * @return colored message
     */
    public String getError(String message) {
        return getColored(message, PromptColor.RED);
    }

    /**
     * Print message with color {@link PromptColor#GREEN}
     *
     * @param message message to print
     */
    public void printSuccess(String message) {
        print(message, PromptColor.GREEN);
    }

    /**
     * Print message with color {@link PromptColor#CYAN}
     *
     * @param message message to print
     */
    public void printInfo(String message) {
        print(message, PromptColor.CYAN);
    }

    /**
     * Print message with color {@link PromptColor#YELLOW}
     *
     * @param message message to print
     */
    public void printWarning(String message) {
        print(message, PromptColor.YELLOW);
    }

    /**
     * Print message with color {@link PromptColor#RED}
     *
     * @param message message to print
     */
    public void printError(String message) {
        print(message, PromptColor.RED);
    }

    /**
     * Print in the console
     *
     * @param message message to print
     */
    public void print(String message) {
        print(message, null);
    }

    /**
     * Print in the console
     *
     * @param message message to print
     * @param color   (optional) prompt color
     */
    public void print(String message, PromptColor color) {
        String toPrint = message;
        if (color != null) {
            toPrint = getColored(message, color);
        }
        SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getTerminal().writer().println(toPrint);
    }

    /**
     * Get ssh authentication containing objects from spring security when configured to 'security'
     *
     * @return authentication from spring authentication, or null of not found in context
     */
    public SshAuthentication getAuthentication() {
        return SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getAuthentication();
    }

    /**
     * Check that one of the roles is in current authorities
     *
     * @param authorizedRoles authorized roles
     * @return true if role found in authorities
     */
    public boolean checkAuthorities(List<String> authorizedRoles) {
        SshAuthentication auth = SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getAuthentication();
        return checkAuthorities(authorizedRoles, auth != null ? auth.getAuthorities() : null, false);
    }

    /**
     * Check that one of the roles is in authorities
     *
     * @param authorizedRoles           authorized roles
     * @param authorities               current authorities
     * @param authorizedIfNoAuthorities whether to return true if no authorities
     * @return true if role found in authorities
     */
    public boolean checkAuthorities(List<String> authorizedRoles, List<String> authorities, boolean authorizedIfNoAuthorities) {
        if (authorities == null) {
            // if authorized only -> return false
            return authorizedIfNoAuthorities;
        }
        for (String authority : authorities) {
            String check = authority;
            if (check.startsWith("ROLE_")) {
                check = check.substring(5);
            }
            if (authorizedRoles.contains(check)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get terminal size
     *
     * @return size
     */
    public Size terminalSize() {
        return SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getTerminal().getSize();
    }

    /**
     * Display percentage on full terminal line
     *
     * @param percentage current value
     * @return percentage line
     */
    public String progress(int percentage) {
        int current = percentage;
        if (current > 100) {
            current = 100;
            LOGGER.warn("Setting percentage to 100 (was: {})", percentage);
        }
        return progress(current, 100);
    }

    /**
     * Display percentage on full terminal line
     *
     * @param current current value
     * @param total   total value
     * @return percentage line
     */
    public String progress(int current, int total) {
        StringBuilder builder = new StringBuilder("[");
        int col = terminalSize().getColumns();
        int max = col - 3;
        if (max < 0) {
            LOGGER.warn("Terminal is too small to print progress [columns={}]", col);
            return "";
        }
        int percentage = current * max / total;

        if (percentage > 0) {
            builder.append(String.format("%" + percentage + "s", " ").replaceAll(" ", "="));
        }
        builder.append(">");
        int left = (max - percentage);
        if (left > 0) {
            builder.append(String.format("%" + left + "s", ""));
        }
        return builder.append("]").toString();
    }

    public void interactive(InteractiveInput input) {
        interactive(input, 1000, true, null);
    }


    // Interactive command which refreshes automatically

    public void interactive(InteractiveInput input, long delay) {
        interactive(input, delay, true, null);
    }

    public void interactive(InteractiveInput input, boolean fullScreen) {
        interactive(input, 1000, fullScreen);
    }

    public void interactive(InteractiveInput input, long delay, boolean fullScreen) {
        interactive(input, delay, fullScreen, null);
    }

    public void interactive(InteractiveInput input, long delay, boolean fullScreen, Size sizeParam) {
        long refreshDelay = delay;
        int rows = 0;
        final int[] maxLines = {rows};
        Terminal terminal = SshShellCommandFactory.SSH_THREAD_CONTEXT.get().getTerminal();
        Display display = new Display(terminal, fullScreen);
        Size size = sizeParam != null ? sizeParam : new Size();
        BindingReader bindingReader = new BindingReader(terminal.reader());

        size.copy(new Size(terminal.getSize().getColumns(), rows));
        long finalRefreshDelay = refreshDelay;
        Terminal.SignalHandler prevHandler = terminal.handle(Terminal.Signal.WINCH, signal -> {
            int previous = size.getColumns();
            size.copy(new Size(terminal.getSize().getColumns(), rows));
            if (size.getColumns() < previous) {
                display.clear();
            }
            maxLines[0] = display(input, display, size, finalRefreshDelay);
        });
        Attributes attr = terminal.enterRawMode();
        try {

            // Use alternate buffer
            if (fullScreen) {
                terminal.puts(InfoCmp.Capability.enter_ca_mode);
                terminal.puts(InfoCmp.Capability.keypad_xmit);
                terminal.puts(InfoCmp.Capability.cursor_invisible);
                terminal.writer().flush();
            }

            long t0 = System.currentTimeMillis();

            KeyMap<Operation> keys = new KeyMap<>();
            keys.bind(Operation.EXIT, "q", ":q", "Q", ":Q");
            keys.bind(Operation.INCREASE_DELAY, "+", "i", "p");
            keys.bind(Operation.DECREASE_DELAY, "-", "d", "m");

            Operation op;
            do {
                maxLines[0] = display(input, display, size, refreshDelay);
                checkInterrupted();

                op = null;

                long delta = ((System.currentTimeMillis() - t0) / refreshDelay + 1) * refreshDelay + t0 - System.currentTimeMillis();

                int ch = bindingReader.peekCharacter(delta);
                if (ch == -1) {
                    op = Operation.EXIT;
                } else if (ch != NonBlockingReader.READ_EXPIRED) {
                    op = bindingReader.readBinding(keys, null, false);
                }
                if (op == null) {
                    continue;
                }

                switch (op) {
                    case INCREASE_DELAY:
                        refreshDelay = refreshDelay + 1000;
                        LOGGER.debug("New refresh delay is now: " + refreshDelay);
                        break;
                    case DECREASE_DELAY:
                        if (refreshDelay > 1000) {
                            refreshDelay = refreshDelay - 1000;
                            LOGGER.debug("New refresh delay is now: " + refreshDelay);
                        } else {
                            LOGGER.warn("Cannot decrease delay under 1000 ms");
                        }
                        break;
                }
            } while (op != Operation.EXIT);
        } catch (InterruptedException ie) {
            // Do nothing
        } finally {
            terminal.setAttributes(attr);
            if (prevHandler != null) {
                terminal.handle(Terminal.Signal.WINCH, prevHandler);
            }
            // Use main buffer
            if (fullScreen) {
                terminal.puts(InfoCmp.Capability.exit_ca_mode);
                terminal.puts(InfoCmp.Capability.keypad_local);
                terminal.puts(InfoCmp.Capability.cursor_visible);
                terminal.writer().flush();
            } else {
                for (int i = 0; i < maxLines[0]; i++) {
                    terminal.writer().println();
                }
            }
        }
    }

    private int display(InteractiveInput input, Display display, Size size, long currentDelay) {
        display.resize(size.getRows(), size.getColumns());
        List<AttributedString> lines = input.getLines(size, currentDelay);
        display.update(lines, 0);
        return lines.size();
    }

    private void checkInterrupted() throws InterruptedException {
        Thread.yield();
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    public enum Operation {
        EXIT,
        INCREASE_DELAY,
        DECREASE_DELAY,
    }

}
