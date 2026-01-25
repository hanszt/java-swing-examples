extern crate rand;
use macroquad::prelude::*;
use rand::seq::SliceRandom;
use std::sync::mpsc::{self, Sender};
use std::thread;

// --- PLUG-IN INTERFACE ---
trait SortAlgorithm {
    fn sort(&self, array: &mut Vec<i32>, sender: Sender<Frame>);
}

struct Stats {
    comparisons: u64,
    swaps: u64,
    active_idx: i32,
    compare_idx: i32,
}

// --- EXAMPLE: BUBBLE SORT ---
struct BubbleSort;
impl SortAlgorithm for BubbleSort {
    fn sort(&self, array: &mut Vec<i32>, sender: Sender<Frame>) {
        let mut comparisons = 0;
        let mut swaps = 0;
        let n = array.len();
        for i in 0..n {
            for j in 0..n - i - 1 {
                comparisons += 1;
                let _ = sender.send(Frame {
                    data: array.clone(),
                    stats: Stats {
                        active_idx: j as i32,
                        compare_idx: (j + 1) as i32,
                        comparisons,
                        swaps,
                    },
                });

                if array[j] > array[j + 1] {
                    array.swap(j, j + 1);
                    swaps += 1;
                }
                // Artificial delay so we can see it
                thread::sleep(std::time::Duration::from_millis(1));
            }
        }
    }
}

// Data sent from Sort Thread -> UI Thread
struct Frame {
    data: Vec<i32>,
    stats: Stats,
}

/// In Rust, using a Channel (std::sync::mpsc) is the most robust way to visualize algorithms. It allows the sorting logic to run at full speed in a background thread while "sending" snapshots of the array to the main thread for rendering.
///
/// This mimics the Producer-Consumer pattern: the Sort Algorithm produces states, and the GUI consumes them to draw.
#[macroquad::main("Rust Sort Visualizer")]
async fn main() {
    let mut array: Vec<i32> = (10..500).step_by(5).collect();
    let n = array.len();
    let mut rng = rand::rng();
    array.shuffle(&mut rng);

    // Create the Channel
    let (tx, rx) = mpsc::channel();

    // Spawn the Sorting Thread
    let mut sort_array = array.clone();
    thread::spawn(move || BubbleSort.sort(&mut sort_array, tx));

    // --- MAIN UI LOOP ---
    let mut current_frame = Frame {
        data: array,
        stats: Stats {
            active_idx: -1,
            compare_idx: -1,
            comparisons: 0,
            swaps: 0,
        },
    };

    loop {
        clear_background(BLACK);

        // Try to get the latest frame from the sort thread
        // We use try_recv so the UI doesn't block/freeze
        while let Ok(new_frame) = rx.try_recv() {
            current_frame = new_frame;
        }
        // Render the bars
        let width = screen_width() / n as f32;
        let stats = &current_frame.stats;
        for (idx, &val) in current_frame.data.iter().enumerate() {
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
