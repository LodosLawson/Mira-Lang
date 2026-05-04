# Mira Programming Language - Full Documentation

Welcome to the official documentation of **Mira**, a high-performance, graphics-oriented programming language designed for both native GPU execution and seamless web rendering.

---

## 1. Core Syntax

### Variables
Variables are defined using the `let` keyword. Mira is dynamically typed but follows a block-scoped structure.
```mira
let name = "Mira Engine";
let version = 5.0;
let isNative = true;
```

### Functions
Functions are first-class citizens in Mira. Defined with the `fn` keyword.
```mira
let add = fn(a, b) {
    return a + b;
};

println(add(10, 20));
```

### Control Flow
Mira supports standard logic structures with an elegant, curly-brace syntax.
```mira
if (x > 10) {
    println("Greater");
} else {
    println("Smaller");
}

let i = 0;
while (i < 5) {
    println(i);
    let i = i + 1;
}
```

---

## 2. Standard Libraries

### `canvas2d` (The Graphics Engine)
This is the heart of Mira. It allows drawing complex graphics that can be rendered either in a Native Window (Java/Swing) or a Web Browser (Canvas API).

| Function | Description |
| :--- | :--- |
| `createCanvas(w, h)` | Initializes the drawing surface. |
| `setFillStyle(color)` | Sets the current drawing color (e.g., "#FF0000"). |
| `fillRect(x, y, w, h)` | Draws a filled rectangle. |
| `strokeRect(x, y, w, h)` | Draws a rectangle outline. |
| `clearCanvas()` | Clears the entire screen. |
| `renderCanvas2d()` | **Crucial:** Pushes the drawing instructions to the GPU/Browser. |

### `math` Library
Built-in mathematical operations for complex simulations.
- `math.sin(x)`, `math.cos(x)`, `math.sqrt(x)`
- `math.random()`: Returns a value between 0 and 1.
- `math.PI`: The constant π.

---

## 3. Dual-Mode Rendering (Native vs Web)

Mira is unique because it uses a **Unified Graphics Protocol**. Your code stays the same, but you can choose where it renders.

1. **Native Mode:** Uses a built-in Java-based renderer with zero dependencies.
2. **Web Mode:** Generates an instructions stream that any modern web browser can execute.

To switch, simply set your environment configuration:
```mira
let RENDER_MODE = "NATIVE"; // or "WEB"
```

---

## 4. Advanced: Object Oriented Programming
Mira supports prototype-based objects and classes.
```mira
class Player {
    fn init(x, y) {
        this.x = x;
        this.y = y;
    }
    fn move(dx) {
        this.x = this.x + dx;
    }
}

let p = new Player(10, 10);
p.move(5);
```

---

## 5. Visual Excellence (The "Wow" Factor)
Mira is optimized for micro-animations and vibrant UI components. Check the `examples/` folder for:
- **3D Solar System Simulation**
- **GPU Particle Systems**
- **Glassmorphism UI Dashboards**

---

*Documentation version 1.0 - Built with ❤️ by LodosLawson*
