function mxTemplate(name, parameter, declaration) {
    this.name = name;
    this.parameter = parameter;
    this.declaration = declaration;
    if (this.onInit != null) {
        this.onInit();
    }
}

mxTemplate.prototype.name = null;
mxTemplate.prototype.parameter = null;
mxTemplate.prototype.declaration = null;

mxTemplate.prototype.insert = function (child, index) {
    if (child != null) {
        if (index == null) {
            index = this.getChildCount();

            if (child.getParent() == this) {
                index--;
            }
        }

        child.removeFromParent();
        child.setParent(this);

        if (this.children == null) {
            this.children = [];
            this.children.push(child);
        } else {
            this.children.splice(index, 0, child);
        }
    }

    return child;
};

mxTemplate.prototype.getChildCount = function () {
    return (this.children == null) ? 0 : this.children.length;
};

mxTemplate.prototype.getParent = function () {
    return this.parent;
};

mxTemplate.prototype.removeFromParent = function () {
    if (this.parent != null) {
        var index = this.parent.getIndex(this);
        this.parent.remove(index);
    }
};

mxTemplate.prototype.setParent = function (parent) {
    this.parent = parent;
};
