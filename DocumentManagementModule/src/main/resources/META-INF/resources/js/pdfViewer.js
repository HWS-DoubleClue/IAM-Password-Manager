var url = location.search.substring(1);

function createPage() {
 	
    var div = document.createElement("canvas");
    document.body.appendChild(div);
    return div;
}


function renderPage(num) {
    pdfDoc.getPage(num).then(function (page) {
        var viewport = page.getViewport({ scale: 2.0 });
        var canvas = createPage();
        var ctx = canvas.getContext('2d');

        canvas.height = viewport.height;
        canvas.width = viewport.width;

        page.render({
            canvasContext: ctx,
            viewport: viewport
        }).promise.then(() => {});
    });
}

pdfjsLib
	.getDocument(pdf)
	.promise.then((data) => {
		initialState.pdfDoc = data;
		console.log('pdfDocument', initialState.pdfDoc);

		pageCount.textContent = initialState.pdfDoc.numPages;

		renderPage();
	})
	.catch((err) => {
		alert(err.message);
	});