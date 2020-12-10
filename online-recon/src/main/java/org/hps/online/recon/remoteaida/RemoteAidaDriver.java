package org.hps.online.recon.remoteaida;

import java.io.IOException;
import java.net.InetAddress;

import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

import hep.aida.dev.IDevTree;
import hep.aida.ref.BatchAnalysisFactory;
import hep.aida.ref.remote.RemoteServer;
import hep.aida.ref.remote.rmi.client.RmiStoreFactory;
import hep.aida.ref.remote.rmi.interfaces.RmiServer;
import hep.aida.ref.remote.rmi.server.RmiServerImpl;

/**
 * Abstract driver for providing remote AIDA functionality
 */
public abstract class RemoteAidaDriver extends Driver {

    static {
        System.setProperty("hep.aida.IAnalysisFactory", BatchAnalysisFactory.class.getName());
        System.setProperty("java.awt.headless", "true");
    }

    protected RemoteServer treeServer;
    protected RmiServer rmiTreeServer;

    protected AIDA aida = AIDA.defaultInstance();
    protected IDevTree tree = (IDevTree) aida.tree();

    static private final int DEFAULT_PORT = 2001;
    protected int port = DEFAULT_PORT;

    static private final String DEFAULT_NAME = "RmiAidaServer";
    protected String serverName = DEFAULT_NAME;

    public RemoteAidaDriver() {
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    protected void endOfData() {
        disconnect();
    }

    protected void startOfData() {

        // HACK: Fixes exceptions from missing AIDA converters
        final RmiStoreFactory rsf = new RmiStoreFactory();

        try {
            connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void process(EventHeader event);

    private final void disconnect() {
        ((RmiServerImpl) rmiTreeServer).disconnect();
        treeServer.close();
    }

    private final void connect() throws IOException {
        String localHost = null;
        try {
            localHost = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String treeBindName = "//"+localHost+":"+port+"/"+serverName;
        System.out.println("Connecting tree server: " + treeBindName);
        try {
            boolean serverDuplex = true;
            treeServer = new RemoteServer(tree, serverDuplex);
            rmiTreeServer = new RmiServerImpl(treeServer, treeBindName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
