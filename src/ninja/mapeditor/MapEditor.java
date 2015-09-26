package ninja.mapeditor;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import ninja.Ninja;
import ninja.entity.Entity;
import ninja.entity.EntityPlayer;
import ninja.entity.EntityTypes;
import ninja.entity.EntityUtil;
import ninja.map.GameMap;
import ninja.textures.Textures;
import ninja.tile.TileMap;
import ninja.tile.Tiles;

public class MapEditor extends JFrame {

	private static final long serialVersionUID = 8615065062512373767L;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MapEditor frame = new MapEditor();
					frame.setTitle(Ninja.frame.getTitle() + " - Map Editor");
					frame.setIconImages(Ninja.frame.getIconImages());
					frame.setLocation(Ninja.frame.getLocation());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JFrame frame = this;

	private MapView mapView;
	private JScrollBar scrollBarZoom;
	private JScrollBar scrollBarH;
	private JScrollBar scrollBarV;
	private JSpinner spinnerLevelWidth;
	private JSpinner spinnerLevelHeight;
	private JButton btnSetLevelSize;
	private JSpinner spinnerBackgroundID;
	private JLabel lblZoom;
	private JLabel lblBackground;
	private JScrollPane scrollPane_1;
	private EntityPropertyTable tableCurEntity;
	private JButton btnRemoveEntity;
	private JButton btnExit;
	private JButton btnTest;
	private JComboBox<Tiles> cbCurBlock;
	private JComboBox<Tiles> cbCurBlockRight;
	private JRadioButton rbEntities;
	private JRadioButton rbBlocks;
	private JLabel lblAddEntity;
	private JComboBox<EntityTypes> cbNewEntity;
	private JCheckBox cbSnapToGrid;
	private JFileChooser fileChooser;
	private JTree treeEntities;
	private DefaultMutableTreeNode treeEntitiesRoot;
	private HashMap<DefaultMutableTreeNode, Object> treeObjects;
	private HashMap<String, DefaultMutableTreeNode> treeFolders;

	public MapEditor() {
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) return true;
				String[] split = file.getName().split("\\.");
				if (split.length >= 2 && (split[split.length - 1].equalsIgnoreCase("map") || split[split.length - 1].equalsIgnoreCase("lnk"))){
					return true;
				}
				return false;
			}

			@Override
			public String getDescription() {
				return "Ninja Map File (*.map)";
			}
		});

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(0, 0, 840, 640);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		scrollBarV = new JScrollBar();
		scrollBarV.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				updateMapView();
			}
		});

		mapView = new MapView(21, 16, 0);
		mapView.setBounds(0, 17, 817, 400);
		mapView.addEntityEventListener(new EntityEvent(){
			@Override
			public void entityAdded(List<Entity> e) {
				DefaultTreeModel model = (DefaultTreeModel)treeEntities.getModel();
				for (Entity entity : e){
					if (entity instanceof EntityPlayer){
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
						model.insertNodeInto(node, treeEntitiesRoot, 0);
						treeObjects.put(node, entity);
					}else{
						String folder = entity.getType().getTypeStr();
						DefaultMutableTreeNode folderNode = treeFolders.get(folder);
						if (folderNode == null){
							folderNode = new DefaultMutableTreeNode(folder);
							treeFolders.put(folder, folderNode);
							model.insertNodeInto(folderNode, treeEntitiesRoot, treeEntitiesRoot.getChildCount());
						}
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(entity);
						treeObjects.put(newNode, entity);
						model.insertNodeInto(newNode, folderNode, folderNode.getChildCount());
					}
					treeEntities.expandRow(0);
				}
			}
			@Override
			public void entityRemoved(List<Entity> e){
				DefaultTreeModel model = (DefaultTreeModel)treeEntities.getModel();
				for (Entity entity : e){
					for(DefaultMutableTreeNode node : treeObjects.keySet()){
						if (treeObjects.get(node) == entity){
							DefaultMutableTreeNode folder = (DefaultMutableTreeNode)node.getParent();
							model.removeNodeFromParent(node);
							if (tableCurEntity.getEntity() == entity)
								tableCurEntity.setEntity(null);
							if (folder.getChildCount() == 0 && !(entity instanceof EntityPlayer)){
								model.removeNodeFromParent(folder);
								for (String s : treeFolders.keySet()){
									if (treeFolders.get(s) == folder){
										treeFolders.remove(s);
										break;
									}
								}
							}
							break;
						}
					}
				}
			}
		});
		mapView.addMapEventListener(new MapEvent(){
			@Override
			public void backgroundChange(int backgroundID) {
				if (spinnerBackgroundID != null)
					spinnerBackgroundID.setValue(backgroundID);
			}
			@Override
			public void tileMapChange(TileMap tileMap) {
				mapView.repaint();
			}
			@Override
			public void zoomChange(double zoom) {
				scrollBarZoom.setValue((int)(zoom * 100));
			}
			@Override
			public void regionChange(Rectangle region) {
				scrollBarH.setValue((int)region.getX());
				scrollBarV.setValue((int)region.getY());
			}
			@Override
			public void cursorInteractModeChanged(int cursorInteractMode) {
				switch (cursorInteractMode){
				case Map.INTERACT_BLOCK: rbBlocks.setSelected(true); break;
				case Map.INTERACT_ENTITY: rbEntities.setSelected(true); break;	
				}
			}
			@Override
			public void newTileMapSizeChange(int width, int height) {
				spinnerLevelWidth.setValue(width);
				spinnerLevelHeight.setValue(height);
			}
		});
		contentPane.add(mapView);
		scrollBarV.setBounds(817, 17, 17, 400);
		contentPane.add(scrollBarV);

		scrollBarH = new JScrollBar();
		scrollBarH.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				updateMapView();
			}
		});
		scrollBarH.setOrientation(JScrollBar.HORIZONTAL);
		scrollBarH.setBounds(0, 416, 817, 17);
		contentPane.add(scrollBarH);

		scrollBarZoom = new JScrollBar();
		scrollBarZoom.setMaximum(800);
		scrollBarZoom.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				setScrollBarMaxMin();
				updateMapView();
			}
		});
		scrollBarZoom.setValue(100);
		scrollBarZoom.setMinimum(50);
		scrollBarZoom.setOrientation(JScrollBar.HORIZONTAL);
		scrollBarZoom.setBounds(43, 0, 774, 17);
		contentPane.add(scrollBarZoom);

		spinnerBackgroundID = new JSpinner();
		spinnerBackgroundID.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				mapView.getMap().setBackgroundID_NE((int) spinnerBackgroundID.getValue());
				mapView.repaint();
			}
		});
		spinnerBackgroundID.setModel(new SpinnerNumberModel(mapView.getMap().getBackgroundID(), 0, Textures.backgroundTextures.size() - 1, 1));
		spinnerBackgroundID.setBounds(81, 444, 68, 20);
		contentPane.add(spinnerBackgroundID);

		spinnerLevelWidth = new JSpinner();
		spinnerLevelWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setScrollBarMaxMin();
				updateMapView();
			}
		});
		spinnerLevelWidth.setModel(new SpinnerNumberModel(mapView.getMap().getTileMap().getWidth(), 1, 1024, 1));
		spinnerLevelWidth.setBounds(81, 473, 68, 20);
		contentPane.add(spinnerLevelWidth);

		JLabel lblLevelWidth = new JLabel("Level Width:");
		lblLevelWidth.setBounds(10, 476, 100, 14);
		contentPane.add(lblLevelWidth);

		spinnerLevelHeight = new JSpinner();
		spinnerLevelHeight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setScrollBarMaxMin();
				updateMapView();
			}
		});
		spinnerLevelHeight.setModel(new SpinnerNumberModel(mapView.getMap().getTileMap().getHeight(), 1, 1024, 1));
		spinnerLevelHeight.setBounds(81, 494, 68, 20);
		contentPane.add(spinnerLevelHeight);

		JLabel lblLevelHeight = new JLabel("Level Height:");
		lblLevelHeight.setBounds(10, 497, 100, 14);
		contentPane.add(lblLevelHeight);

		btnSetLevelSize = new JButton("Set Level Size");
		btnSetLevelSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapView.map.setNewTileSize();
				setScrollBarMaxMin();
				updateMapView();
				mapView.repaint();
			}
		});
		btnSetLevelSize.setBounds(10, 515, 139, 23);
		contentPane.add(btnSetLevelSize);

		lblZoom = new JLabel("Zoom:");
		lblZoom.setBounds(6, 1, 35, 14);
		contentPane.add(lblZoom);

		lblBackground = new JLabel("Background:");
		lblBackground.setBounds(10, 447, 100, 14);
		contentPane.add(lblBackground);

		final JCheckBox cbShowGrid = new JCheckBox("Show Grid");
		cbShowGrid.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				mapView.map.showGrid = cbShowGrid.isSelected();
				mapView.repaint();
			}
		});
		cbShowGrid.setSelected(true);
		cbShowGrid.setBounds(10, 555, 97, 23);
		contentPane.add(cbShowGrid);

		final JCheckBox chckbxShowEntities = new JCheckBox("Show Entities");
		chckbxShowEntities.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				mapView.map.showEntities = chckbxShowEntities.isSelected();
				mapView.repaint();
			}
		});
		chckbxShowEntities.setSelected(true);
		chckbxShowEntities.setBounds(10, 578, 97, 23);
		contentPane.add(chckbxShowEntities);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(154, 445, 2, 155);
		contentPane.add(separator);

		rbBlocks = new JRadioButton("Edit Blocks");
		rbBlocks.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				if (rbBlocks.isSelected())
					mapView.map.setCursorInteractModeNE(Map.INTERACT_BLOCK);
			}
		});
		rbBlocks.setSelected(true);
		rbBlocks.setBounds(157, 443, 92, 23);
		contentPane.add(rbBlocks);

		cbCurBlock = new JComboBox<Tiles>();
		cbCurBlock.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				rbBlocks.setSelected(true);
			}
		});
		cbCurBlock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapView.map.curTile = mapView.leftClickBlock = (Tiles)cbCurBlock.getSelectedItem();
			}
		});
		cbCurBlock.setModel(new DefaultComboBoxModel<Tiles>(Tiles.values()));
		cbCurBlock.setSelectedIndex(1);
		cbCurBlock.setBounds(159, 484, 90, 20);
		contentPane.add(cbCurBlock);
		mapView.map.curTile = (Tiles)cbCurBlock.getSelectedItem();

		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(254, 445, 2, 155);
		contentPane.add(separator_1);

		rbEntities = new JRadioButton("Edit Entities");
		rbEntities.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (rbEntities.isSelected())
					mapView.map.setCursorInteractModeNE(Map.INTERACT_ENTITY);
			}
		});
		rbEntities.setBounds(259, 443, 92, 23);
		contentPane.add(rbEntities);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				Ninja.frame.setVisible(true);
				Ninja.gameCanvas.unpause();
			}
		});

		ButtonGroup radioButtons = new ButtonGroup();
		radioButtons.add(rbBlocks);
		radioButtons.add(rbEntities);

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(549, 444, 176, 157);
		contentPane.add(scrollPane_1);

		tableCurEntity = new EntityPropertyTable(null);
		tableCurEntity.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				mapView.repaint();
			}
		});
		tableCurEntity.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_1.setViewportView(tableCurEntity);
		tableCurEntity.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		tableCurEntity.getActionMap().put("Enter", new AbstractAction() {
			private static final long serialVersionUID = 5900924892448534375L;
			@Override
			public void actionPerformed(ActionEvent ae) {
				((JTable)ae.getSource()).getCellEditor().stopCellEditing();
			}
		});


		btnRemoveEntity = new JButton("Remove Entity");
		btnRemoveEntity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapView.getMap().removeEntity(tableCurEntity.getEntity());
			}
		});
		btnRemoveEntity.setBounds(361, 579, 188, 23);
		contentPane.add(btnRemoveEntity);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					try {
						mapView.getMap().reset();
						mapView.setMap(new Map(mapView, GameMap.fromFile(fileChooser.getSelectedFile())));
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Error opening file!");
					}
				}
			}
		});
		btnOpen.setBounds(735, 532, 89, 23);
		contentPane.add(btnOpen);

		JButton btnSave = new JButton("Save As");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION){
					String filename = fileChooser.getSelectedFile().getAbsolutePath();
					String[] splitName = fileChooser.getSelectedFile().getName().split("\\.");
					if (splitName.length < 2) filename += ".map";
					fileChooser.setSelectedFile(new File(filename));
					File saveTo = fileChooser.getSelectedFile();
					if (saveTo.exists()){
						saveTo.delete();
					}
					try {
						saveTo.createNewFile();
						BufferedWriter writer = new BufferedWriter(new FileWriter(saveTo));
						writer.write(mapView.map.getFileString());
						writer.close();
						JOptionPane.showMessageDialog(frame, "File saved!");
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Error saving file!");
					}
				}
			}
		});
		btnSave.setBounds(735, 555, 89, 23);
		contentPane.add(btnSave);

		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(frame, "Create a new map?", frame.getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION){
					mapView.map.reset();
				}
			}
		});
		btnNew.setBounds(735, 509, 89, 23);
		contentPane.add(btnNew);

		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		btnExit.setBounds(735, 578, 89, 23);
		contentPane.add(btnExit);

		btnTest = new JButton("Test");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (mapView.getMap().getPlayer() == null){
					JOptionPane.showMessageDialog(frame, "Map needs a Player entity!");
				}else{
					frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					new MapTester(mapView.getMap().toGameMap()).setVisible(true);
					frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		btnTest.setBounds(735, 486, 89, 23);
		contentPane.add(btnTest);

		JLabel lblLeftClick = new JLabel("Left Click:");
		lblLeftClick.setBounds(159, 467, 90, 14);
		contentPane.add(lblLeftClick);

		cbCurBlockRight = new JComboBox<Tiles>();
		cbCurBlockRight.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				rbBlocks.setSelected(true);
			}
		});
		cbCurBlockRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapView.rightClickBlock = (Tiles)cbCurBlockRight.getSelectedItem();
			}
		});
		cbCurBlockRight.setModel(new DefaultComboBoxModel<Tiles>(Tiles.values()));
		cbCurBlockRight.setSelectedIndex(0);
		cbCurBlockRight.setBounds(159, 527, 90, 20);
		contentPane.add(cbCurBlockRight);

		JLabel lblRightClick = new JLabel("Right Click:");
		lblRightClick.setBounds(159, 510, 90, 14);
		contentPane.add(lblRightClick);

		lblAddEntity = new JLabel("Add Entity:");
		lblAddEntity.setBounds(260, 467, 90, 14);
		contentPane.add(lblAddEntity);

		cbNewEntity = new JComboBox<EntityTypes>();
		cbNewEntity.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				rbEntities.setSelected(true);
			}
		});
		cbNewEntity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapView.map.curNewEntity = EntityUtil.fromType(mapView.map, ((EntityTypes)cbNewEntity.getSelectedItem()));
			}
		});
		cbNewEntity.setModel(new DefaultComboBoxModel<EntityTypes>(EntityTypes.values()));
		cbNewEntity.setBounds(260, 484, 91, 20);
		contentPane.add(cbNewEntity);
		cbNewEntity.setSelectedItem(null);

		cbSnapToGrid = new JCheckBox("Snap to Grid");
		cbSnapToGrid.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				rbEntities.setSelected(true);
			}
		});
		cbSnapToGrid.setSelected(true);
		cbSnapToGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapView.entitySnapToGrid = cbSnapToGrid.isSelected();
			}
		});
		cbSnapToGrid.setBounds(256, 505, 100, 23);
		contentPane.add(cbSnapToGrid);
		mapView.entitySnapToGrid = cbSnapToGrid.isSelected();

		JScrollPane spEntities = new JScrollPane();
		spEntities.setBounds(362, 444, 186, 135);
		contentPane.add(spEntities);

		treeEntities = new JTree();
		treeEntities.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent arg0) {
				treeEntities.expandRow(0);
			}
			public void treeExpanded(TreeExpansionEvent arg0) {}
		});
		treeEntities.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Object selected = treeObjects.get(e.getPath().getLastPathComponent());
				if (selected instanceof Entity){
					tableCurEntity.setEntity((Entity)selected);
					mapView.getMap().selectedEntity = (Entity)selected;
					mapView.repaint();
				}else{
					tableCurEntity.setEntity(null);
					mapView.getMap().selectedEntity = null;
					mapView.repaint();
				}
			}
		});
		treeEntitiesRoot = new DefaultMutableTreeNode("Entities");
		treeObjects = new HashMap<DefaultMutableTreeNode, Object>();
		treeFolders = new HashMap<String, DefaultMutableTreeNode>();
		treeEntities.setModel(new DefaultTreeModel(treeEntitiesRoot));
		treeEntities.setCellRenderer(new EntityIconRenderer());
		spEntities.setViewportView(treeEntities);

		setScrollBarMaxMin();
		updateMapView();
	}

	public void updateMapView() {
		mapView.setRegionXY_NE(
				scrollBarH.getValue() / mapView.getZoom() / mapView.getZoom(), 
				scrollBarV.getValue() / mapView.getZoom() / mapView.getZoom());
		mapView.map.setBackgroundID_NE(spinnerBackgroundID == null ? 0 : (int) spinnerBackgroundID.getValue());
		if (spinnerLevelWidth != null) {
			mapView.map.setNewTileWidthNE((int)spinnerLevelWidth.getValue());
		}
		if (spinnerLevelHeight != null) {
			mapView.map.setNewTileHeightNE((int)spinnerLevelHeight.getValue());
		}
		mapView.setZoomNE(scrollBarZoom.getValue() / 100.);
		mapView.repaint();
	}

	public void setScrollBarMaxMin() {
		scrollBarV.setMaximum((int) Math.max(0, mapView.getMap().getHeight() * mapView.getZoom() - mapView.getHeight()));
		scrollBarH.setMaximum((int) Math.max(0, mapView.getMap().getWidth() * mapView.getZoom() - mapView.getWidth()));
	}

	public Object getDefaultValue(Class<?> test) {
		if (test == Integer.class) {
			return 0;
		} else if (test == Double.class) {
			return 0.0D;
		} else if (test == String.class) {
			return "";
		}
		return null;
	}
}

class EntityIconRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 544161844026576644L;

	public EntityIconRenderer() {
		super();
	}
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		DefaultTreeCellRenderer result = (DefaultTreeCellRenderer)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		Object nodeObj = ((DefaultMutableTreeNode)value).getUserObject();
		if (nodeObj instanceof Entity) {
			result.setIcon(new ImageIcon(((Entity)nodeObj).getIcon()));
		}else{
			if (expanded){
				result.setIcon(UIManager.getIcon("Tree.openIcon"));
			}else{
				result.setIcon(UIManager.getIcon("Tree.closedIcon"));
			}
		}
		return result;
	}
}