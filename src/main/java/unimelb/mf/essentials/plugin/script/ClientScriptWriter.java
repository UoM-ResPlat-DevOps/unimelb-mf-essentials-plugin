package unimelb.mf.essentials.plugin.script;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import unimelb.mf.essentials.plugin.util.ServerDetails;

public abstract class ClientScriptWriter extends PrintWriter {

    public static enum LineSeparator {
        WINDOWS("\r\n"), UNIX("\n");
        private String _value;

        LineSeparator(String value) {
            _value = value;
        }

        public String value() {
            return _value;
        }
    }

    private ServerDetails _serverDetails;

    private String _token;
    private String _tokenApp;

    private LineSeparator _lineSeparator = null;
    private boolean _autoFlush;

    private boolean _closed = false;

    protected ClientScriptWriter(ServerDetails serverDetails, String token, String tokenApp, OutputStream os,
            boolean autoFlush, LineSeparator lineSeparator) throws Throwable {
        super(new BufferedWriter(new OutputStreamWriter(os, "UTF-8")), autoFlush);
        _serverDetails = serverDetails;
        _token = token;
        _tokenApp = tokenApp;
        _autoFlush = autoFlush;
        _lineSeparator = lineSeparator != null && !lineSeparator.value().equals(System.getProperty("line.separator"))
                ? lineSeparator
                : null;
        initialize();
        writeHead();
    }

    protected void initialize() {

    }

    public ServerDetails server() {
        return _serverDetails;
    }

    public String token() {
        return _token;
    }

    public String tokenApp() {
        return _tokenApp;
    }

    @Override
    public void println() {
        if (_lineSeparator == null) {
            super.println();
        } else {
            super.write(_lineSeparator.value());
            if (_autoFlush) {
                super.flush();
            }
        }
    }

    public abstract TargetOS targetOS();

    protected void writeHead() throws Throwable {

    }

    protected void writeTail() {

    }

    public void close() {
        if (!_closed) {
            try {
                writeTail();
            } finally {
                _closed = true;
                super.close();
            }
        } else {
            super.close();
        }
    }
}
