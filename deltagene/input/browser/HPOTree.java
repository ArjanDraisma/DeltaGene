package deltagene.input.browser;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import deltagene.input.data.HPONumber;

public class HPOTree extends JTree {
	private static final long serialVersionUID = 1L;

    Stack<Stack<TreePath>> expandedStack = new Stack<Stack<TreePath>>();
    Hashtable<TreePath, Boolean> expandedState = new Hashtable<TreePath, Boolean>();
    private static final int TEMP_STACK_SIZE = 11;

    
	/**
	 * @param node the root node
	 */
	public HPOTree(TreeNode node) {
		super(node);
		DefaultTreeCellRenderer customRenderer = 
				new DefaultTreeCellRenderer() {
				
				private static final long serialVersionUID = 2L;

				public Component getTreeCellRendererComponent(
						JTree tree, Object value, boolean sel,
						boolean expanded, boolean leaf, int row,
						boolean hasFocus) {
					if ((value != null) && value instanceof TreeNode) {
						HPONumber hpoNode = (HPONumber)value;
						this.setText(hpoNode.hpo()
								+" - "+hpoNode.phenotype());
						this.selected = sel;
						if (sel) {
							super.setBackground(getBackgroundSelectionColor());
							setForeground(this.getTextSelectionColor());
						}else{
							super.setBackground(getBackgroundNonSelectionColor());
							setForeground(this.getTextNonSelectionColor());
						}
						return this;
					}
					return new JLabel("null");
				}
			};
			this.setCellRenderer(customRenderer);
	}

	/**
	 * For some reason, having this here makes expanding
	 * a lot of nodes a lot faster as opposed to using JTree's
	 * native setExpandedState
	 */
	@Override
	protected void setExpandedState(TreePath path, boolean state) {
	    if(path != null) {
            // Make sure all parents of path are expanded.
            Stack<TreePath>         stack;
            TreePath      parentPath = path.getParentPath();

            if (expandedStack.size() == 0) {
                stack = new Stack<TreePath>();
            }
            else {
                stack = (Stack<TreePath>)expandedStack.pop();
            }

            try {
                while(parentPath != null) {
                    if(isExpanded(parentPath)) {
                        parentPath = null;
                    }
                    else {
                        stack.push(parentPath);
                        parentPath = parentPath.getParentPath();
                    }
                }
                for(int counter = stack.size() - 1; counter >= 0; counter--) {
                    parentPath = (TreePath)stack.pop();
                    if(!isExpanded(parentPath)) {
                        try {
                            fireTreeWillExpand(parentPath);
                        } catch (ExpandVetoException eve) {
                            // Expand vetoed!
                            return;
                        }
                        expandedState.put(parentPath, Boolean.TRUE);
                        fireTreeExpanded(parentPath);
                        if (accessibleContext != null) {
                            ((AccessibleJTree)accessibleContext).
                                              fireVisibleDataPropertyChange();
                        }
                    }
                }
            }
            finally {
                if (expandedStack.size() < TEMP_STACK_SIZE) {
                    stack.removeAllElements();
                    expandedStack.push(stack);
                }
            }
            if(!state) {
                // collapse last path.
                Object          cValue = expandedState.get(path);

                if(cValue != null && ((Boolean)cValue).booleanValue()) {
                    try {
                        fireTreeWillCollapse(path);
                    }
                    catch (ExpandVetoException eve) {
                        return;
                    }
                    expandedState.put(path, Boolean.FALSE);
                    fireTreeCollapsed(path);
                    if (removeDescendantSelectedPaths(path, false) &&
                        !isPathSelected(path)) {
                        // A descendant was selected, select the parent.
                        addSelectionPath(path);
                    }
                    if (accessibleContext != null) {
                        ((AccessibleJTree)accessibleContext).
                                    fireVisibleDataPropertyChange();
                    }
                }
            }
            else {
                // Expand last path.
                Object          cValue = expandedState.get(path);

                if(cValue == null || !((Boolean)cValue).booleanValue()) {
                    try {
                        fireTreeWillExpand(path);
                    }
                    catch (ExpandVetoException eve) {
                        return;
                    }
                    expandedState.put(path, Boolean.TRUE);
                    fireTreeExpanded(path);
                    if (accessibleContext != null) {
                        ((AccessibleJTree)accessibleContext).
                                          fireVisibleDataPropertyChange();
                    }
                }
            }
        }
    }

	
	
	@Override
	public void expandPath(TreePath path) {
		TreeModel  model = getModel();
		
		if(path != null && model != null &&
			!model.isLeaf(path.getLastPathComponent())
			&& !isExpanded(path)) {
			setExpandedState(path, true);
		}
	}
	
	public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        if (root!=null) {
            // Traverse tree from root
            expandAll(tree, new TreePath(root), expand);
        }
    }

    /**
     * @return Whether an expandPath was called for the last node in the parent path
     */
    private boolean expandAll(JTree tree, TreePath parent, boolean expand) {
    	TreeUI ui = this.getUI();
    	this.setUI(null);
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            boolean childExpandCalled = false;
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                childExpandCalled = expandAll(tree, path, expand) || childExpandCalled; // the OR order is important here, don't let childExpand first. func calls will be optimized out !
            }

            if (!childExpandCalled) { // only if one of the children hasn't called already expand
                // Expansion or collapse must be done bottom-up, BUT only for non-leaf nodes
                if (expand) {
                    tree.expandPath(parent);
                } else {
                    tree.collapsePath(parent);
                }
            }
            this.setUI(ui);
            return true;
        } else {
            return false;
        }
    }
}