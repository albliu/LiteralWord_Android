function jumpToElement(id) {
    var elem = document.getElementById(id);
    var x = 0;
    var y = 0;

    while (elem != null) {
        x += elem.offsetLeft;
        y += elem.offsetTop;
        elem = elem.offsetParent;
    }
    
    window.scrollTo(x,y);
}

function highlight(id) {
	var elem = document.getElementById(id);
	elem.className = 'active';
	window.verse.clickVerse(elem.id, elem.innerText, elem.className);
	
	var dchild = elem.getElementsByTagName('div');
	for (var i = 0; i < dchild.length; i++) {
		dchild[i].firstChild.className = elem.className;
	}
}

function unhighlight(id) {
	var elem = document.getElementById(id);
	elem.className = '';
	window.verse.clickVerse(elem.id, elem.innerText, elem.className);

	var dchild = elem.getElementsByTagName('div');
	for (var i = 0; i < dchild.length; i++) {
		dchild[i].firstChild.className = elem.className;
	}
}

function highlightPoint(x, y) {

	var elem = document.elementFromPoint(x,y);
	while (elem.tagName != "V") {
		elem = elem.parentNode;
	} 
	
	if (elem.className == "active") { elem.className = '' } else { elem.className = 'active' };
	
	
	window.verse.clickVerse(elem.id, elem.innerText, elem.className);
	
	var dchild = elem.getElementsByTagName('div');
	for (var i = 0; i < dchild.length; i++) {
		dchild[i].firstChild.className = elem.className;
	}
		 
	
}