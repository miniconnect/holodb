package hu.webarticum.holodb.regex.algorithm;

import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class TransformResult {

    final boolean wasChanged;

    final ImmutableList<TreeNode> resultingChildren;

    TransformResult(boolean wasChanged, ImmutableList<TreeNode> resultingChildren)  {
        this.wasChanged = wasChanged;
        this.resultingChildren = resultingChildren;
    }

}
