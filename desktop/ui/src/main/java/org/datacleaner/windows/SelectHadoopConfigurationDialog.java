/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Provider;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jfree.ui.tabbedui.VerticalLayout;

public class SelectHadoopConfigurationDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final JList<String> _serverList;
    private final JButton _okButton;
    private final JButton _optionsButton;
    private String _selectedConfiguration;
    private final LinkedList<String> _mappedServers;
    private static final ImageManager _imageManager = ImageManager.get();
    private final Provider<HadoopConfigurationsOptionsDialog> _hadoopOptionsDialogProvider;

    
    public SelectHadoopConfigurationDialog(WindowContext windowContext, ServerInformationCatalog serverInformationCatalog, Provider<HadoopConfigurationsOptionsDialog> hadoopOptionsDialogProvider) {
         super(windowContext); 
         
         _hadoopOptionsDialogProvider = hadoopOptionsDialogProvider; 
         //It needs to be modal. Otherwise there will be null for selected Configuration
         setModal(true);
         setResizable(true);
        // It's important to keep the order of the elements.
        _mappedServers = new LinkedList<String>();
        final String[] serverNames = getMappedServers(serverInformationCatalog.getServerNames(), _mappedServers);
        _selectedConfiguration = null; 
        _serverList = new JList<String>(serverNames);
        _serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _serverList.setLayoutOrientation(JList.VERTICAL);
        _serverList.setSelectedIndex(serverNames.length-1);
        _serverList.setBorder(WidgetUtils.BORDER_WIDE_WELL);

          
        _okButton = WidgetFactory.createPrimaryButton("OK",  IconUtils.ACTION_FORWARD);
        _okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = _serverList.getSelectedIndex();
                _selectedConfiguration = serverInformationCatalog
                        .getServerNames()[selectedIndex];
              dispose();
            }
        });
        _optionsButton = WidgetFactory.createDefaultButton("Options");
        _optionsButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectHadoopConfigurationDialog.this.close();
                final HadoopConfigurationsOptionsDialog hadoopConfigurationsOptionsDialog = _hadoopOptionsDialogProvider
                        .get();
                hadoopConfigurationsOptionsDialog.setVisible(true);
            }
        });

      
    }

    /**
     * We avoid having HadoopResource.DEFAULT_CLUSTERREFERENCE(
     * "org.datacleaner.hadoop.environment") as a server name. We write
     * "default" instead. 
     */
    private String[] getMappedServers(String[] serverNames, LinkedList<String> mappedServers) {

        for (int i = 0; i < serverNames.length; i++) {
            final String serverName = serverNames[i];
            if (serverName.equals(HadoopResource.DEFAULT_CLUSTERREFERENCE)) {
                mappedServers.add(0, "(default)");
            } else {
                mappedServers.add(serverName);
            }
        }
        return mappedServers.toArray(new String[serverNames.length]);
    }

    public String getSelectedConfiguration() {
        return _selectedConfiguration;
    }

    @Override
    public String getWindowTitle() {
        return "Select Hadoop configuration";
    }

    @Override
    protected String getBannerTitle() {
        return "Hadoop configurations";
    }

    @Override
    protected int getDialogWidth() {
        return 400;
    }
    
    @Override
    protected JComponent getDialogContent() {
       
        final DCPanel contentPanel = new DCPanel();
        contentPanel.setLayout(new GridLayout());
        
        final DCPanel listPanel = new DCPanel(); 
        listPanel.setBackground(WidgetUtils.COLOR_WELL_BACKGROUND);
        listPanel.setLayout(new VerticalLayout());
        listPanel.add(_serverList);
        

        final DCLabel label = DCLabel.dark("Select Hadoop configuration: ");
        label.setFont(WidgetUtils.FONT_HEADER2);
        WidgetUtils.addToGridBag(label, contentPanel, 0, 0);
        WidgetUtils.addToGridBag(listPanel, contentPanel, 0, 1);
        WidgetUtils.addToGridBag(_optionsButton, contentPanel, 1, 1, GridBagConstraints.NORTH);
        WidgetUtils.addToGridBag(_okButton, contentPanel, 1, 1, GridBagConstraints.SOUTH);
        
        final JScrollPane scrolleable = WidgetUtils.scrolleable(contentPanel);
        scrolleable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        final Image hadoopImage = _imageManager.getImage(IconUtils.FILE_HDFS);
        final DCBannerPanel banner = new DCBannerPanel(hadoopImage, "Select Hadoop Cluster");
        
        final DCPanel outerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND); 
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(banner, BorderLayout.NORTH); 
        outerPanel.add(scrolleable, BorderLayout.CENTER); 
        outerPanel.setPreferredSize(getDialogWidth(), 300); 
        return outerPanel; 
    }
    
    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation(HadoopResource.DEFAULT_CLUSTERREFERENCE,
                "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client"));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);


        String hadoopConfiguration = null; 
        WindowContext windowContext = new DCWindowContext(null, null, null);
        final SelectHadoopConfigurationDialog selectHadoopConfigurationDialog = new SelectHadoopConfigurationDialog(windowContext, 
                serverInformationCatalog, null);
        selectHadoopConfigurationDialog.setVisible(true);
        selectHadoopConfigurationDialog.pack();

        System.out.println(hadoopConfiguration);

    }

   
}
