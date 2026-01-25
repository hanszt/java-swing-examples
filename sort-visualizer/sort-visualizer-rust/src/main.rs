extern crate rand;
use macroquad::prelude::*;
use rand::prelude::*;

struct Stats {
    comparisons: u64,
    swaps: u64,
    active_idx: i32,
    compare_idx: i32,
}

// --- VISUALIZATION ENGINE ---
#[macroquad::main("Rust Sort Visualizer")]
async fn main() {
    let mut array: Vec<i32> = (10..500).step_by(5).collect();
    let n = array.len();
    let mut rng = rand::rng();
    array.shuffle(&mut rng);

    let mut stats = Stats {
        comparisons: 0,
        swaps: 0,
        active_idx: -1,
        compare_idx: -1,
    };

    // For simplicity in this demo, we'll step through the algorithm manually
    // or use a separate thread. Let's do a basic iterative Bubble Sort inside the loop.
    let mut i = 0;
    let mut j = 0;

    loop {
        clear_background(BLACK);

        // --- DRAWING LOGIC ---
        let width = screen_width() / n as f32;
        for (idx, &val) in array.iter().enumerate() {
            let color = if idx as i32 == stats.active_idx {
                RED
            } else if idx as i32 == stats.compare_idx {
                YELLOW
            } else {
                // Rainbow Effect
                let hue = val as f32 / 500.0;
                Color::from_rgba((hue * 255.0) as u8, 200, 255 - (hue * 255.0) as u8, 255)
            };

            draw_rectangle(
                idx as f32 * width,
                screen_height() - val as f32,
                width - 1.0,
                val as f32,
                color,
            );
        }

        // --- SORTING STEP (Bubble Sort Logic) ---
        if i < n {
            if j < n - i - 1 {
                stats.active_idx = j as i32;
                stats.compare_idx = (j + 1) as i32;
                stats.comparisons += 1;
                if array[j] > array[j + 1] {
                    array.swap(j, j + 1);
                    stats.swaps += 1;
                }
                j += 1;
            } else {
                j = 0;
                i += 1;
            }
        }

        draw_text(
            &format!("Comps: {} | Swaps: {}", stats.comparisons, stats.swaps),
            20.0,
            30.0,
            30.0,
            WHITE,
        );

        next_frame().await;
    }
}
