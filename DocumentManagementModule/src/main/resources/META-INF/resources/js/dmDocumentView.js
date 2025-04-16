
const form = document.getElementById('documentForm');
const overlay = document.getElementById('dragOverlay');

// Drag counter to track dragenter and dragleave events
let dragCounter = 0;

// Prevent default behavior for drag events
const preventDefaults = (event) => {
    event.preventDefault();
    event.stopPropagation();
};

// Add drag-and-drop event listeners
form.addEventListener('dragenter', (event) => {
    preventDefaults(event);
    dragCounter++;
    overlay.classList.add('active'); // Show overlay
});

form.addEventListener('dragleave', (event) => {
    preventDefaults(event);
    dragCounter--;
    if (dragCounter === 0) {
        overlay.classList.remove('active'); // Hide overlay
    }
});

form.addEventListener('dragover', (event) => {
    preventDefaults(event); // Necessary to allow drop
});

form.addEventListener('drop', (event) => {
    preventDefaults(event);
    dragCounter = 0; // Reset drag counter
    overlay.classList.remove('active');
});
function test() {
    console.log('test')
}

function getParent () {
	alert (window.parent.name);
}

function updateParent () {
//	alert (window.parent.location);
	parent.location.reload();
}