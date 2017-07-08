package org._2333.cyls;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 2333
 */
public abstract class TreeNode {
    public class PathDoesNotExistException extends Exception {
        PathDoesNotExistException() {
            super();
        }
    } //路径不存在时抛出

    /**
     * @param str 参数
     * @throws Exception 抛出的异常
     */
    public abstract void run(String str) throws Exception;

    private String str_; //该节点路径名
    private TreeNode parent_; //父节点
    private List<TreeNode> children_; //子节点

    public TreeNode(@NotNull String str, TreeNode parent) {
        parent_ = parent;
        str_ = str;
        if (parent_ != null)parent_.children_.add(this);
        children_ = new ArrayList<>();
    }

    /**
     * @param path 路径
     * @return 该路径下的子节点
     * @throws PathDoesNotExistException 当路径不存在时抛出异常
     */
    public TreeNode getchild(String path) throws PathDoesNotExistException {
        int i;
        if (path.contains(".")) { //如果不是最下层路径
            i = path.indexOf(".");
            String nextPath = path.substring(0, i); //获取该层路径名
            for (TreeNode child : children_) {
                if (child.str_.equals(nextPath)) { //寻找子节点
                    return child.getchild(path.substring(i + 1)); //递归确定
                }
            }
            throw new PathDoesNotExistException(); //没有找到相应的子节点，抛出异常
        } else { //当前为底层路径
            for (TreeNode child : children_) {
                if (child.str_.equals(path)) { //寻找子节点
                    return child; //就是所要的节点
                }
            }
            throw new PathDoesNotExistException(); //没有找到相应的子节点，抛出异常
        }
    }

    /**
     * @param pathAndParameter 路径+一个空格+参数
     * @throws Exception 可能抛出的异常
     */
    public void runPath(String pathAndParameter) throws Exception {
        int i;
        if (pathAndParameter.contains(".")&&(!pathAndParameter.contains(" ")||pathAndParameter.indexOf(' ')>pathAndParameter.indexOf('.'))) {  //不为底层路径
            i = pathAndParameter.indexOf('.');
            String nextPath = pathAndParameter.substring(0, i); //获取该层路径名
            for (TreeNode child : children_) {
                if (child.str_.equals(nextPath)) { //寻找子节点
                    child.runPath(pathAndParameter.substring(i + 1)); //递归确定
                    return;
                }
            }
            throw new PathDoesNotExistException(); //没有找到相应的子节点，抛出异常
        } else { //当前为底层路径
            if (pathAndParameter.contains(" ")) { //有参数
                i = pathAndParameter.indexOf(' ');
                String nextPath = pathAndParameter.substring(0, i); //获取该层路径名
                for (TreeNode child : children_) {
                    if (child.str_.equals(nextPath)) { //寻找子节点
                        child.run(pathAndParameter.substring(i + 1)); //执行相应函数
                        return;
                    }
                }
                throw new PathDoesNotExistException(); //没有找到相应的子节点，抛出异常
            } else { //无参数
                String nextPath = pathAndParameter;
                for (TreeNode child : children_) {
                    if (child.str_.equals(nextPath)) { //寻找子节点
                        child.run(""); //执行相应函数
                        return;
                    }
                }
                throw new PathDoesNotExistException();//没有找到相应的子节点，抛出异常
            }
        }
    }

    public void runRegexPath(String Parameter) throws Exception {
        for (TreeNode child : children_) {
            if (Parameter.matches(child.str_)) { //寻找子节点
                child.runRegexPath(Parameter); //递归确定
                return;
            }
        }
        run(Parameter);//没有匹配的子节点，则调用自身的函数
    }

    public String getStr() {
        return str_;
    }

    public void setStr_(@NotNull String str) {
        str_ = str;
    }

    public TreeNode getParent() {
        return parent_;
    }

    public void setaparent(TreeNode parent) {
        parent_ = parent;
    }

    public List<TreeNode> getChildren() {
        return children_;
    }

    public void setChildren(List<TreeNode> children) {
        children_ = children;
    }
}
