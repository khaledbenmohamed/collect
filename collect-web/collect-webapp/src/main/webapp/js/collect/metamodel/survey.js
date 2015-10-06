Collect.Metamodel = function() {};

Collect.Metamodel.Survey = function(json) {
	var $this = this;
	
	$.extend($this, json);
	
	$this.nodeById = new Array();
	$this.init();
};

Collect.Metamodel.Survey.prototype.init = function() {
	var $this = this;
	this.traverse(function(nodeDef) {
		$this.nodeById[nodeDef.id] = nodeDef;
	});
};

Collect.Metamodel.Survey.prototype.getDefinition = function(id) {
	var node = this.nodeById[id];
	return node;
};

Collect.Metamodel.Survey.prototype.getKeyDefinitions = function(rootEntity) {
	var result = new Array();
	var stack = new Array();
	stack.push(rootEntity);
	while (stack.length > 0) {
		var nodeDef = stack.pop();
		if (nodeDef.type == "ATTRIBUTE" && nodeDef.key) {
			result.push(nodeDef);
		} else if(nodeDef.type == "ENTITY" && (nodeDef.root || ! nodeDef.multiple)) {
			nodeDef.children.forEach(function(childDef) {
				stack.push(childDef);
			});
		}
	}
	return result;
};

Collect.Metamodel.Survey.prototype.traverse = function(fun) {
	var stack = new Array();
	stack = stack.concat(this.rootEntities);
	while (stack.length > 0) {
		var nodeDef = stack.pop();
		fun(nodeDef);
		if (nodeDef.type == "ENTITY") {
			nodeDef.children.forEach(function(childDef) {
				stack.push(childDef);
			});
		}
	}
};
