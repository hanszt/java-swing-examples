extern crate rand;

use macroquad::hash;
use macroquad::math::i32;
use macroquad::prelude::*;
use macroquad::ui::root_ui;
use rand::seq::SliceRandom;
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::mpsc::{self, Sender};
use std::sync::Arc;
use std::thread;
use std::time::Duration;

// --- PLUG-IN INTERFACE ---
trait SortAlgorithm {
    fn run_sort(
        &self,
        array: &mut Vec<i32>,
        sender: Sender<Frame>,
        stats: &mut Stats,
        delay_ms: Arc<AtomicU64>,
    );

    // 2. This is the "Plug-in" entry point with default logic
    fn sort(&self, array: &mut Vec<i32>, sender: Sender<Frame>, delay_ms: Arc<AtomicU64>) {
        // Initialize the variables
        let mut stats = Stats {
            comparisons: 0,
            swaps: 0,
            active_idx: -1,
            compare_idx: -1,
            is_done: false,
        };
        self.run_sort(array, sender.clone(), &mut stats, delay_ms);
        // Signal the sorting is completed
        stats.is_done = true;
        stats.active_idx = -1;
        stats.compare_idx = -1;
        let _ = sender.send(Frame {
            data: array.clone(),
            stats,
        });
    }
}

#[derive(Clone)]
struct Stats {
    comparisons: u64,
    swaps: u64,
    active_idx: i32,
    compare_idx: i32,
    is_done: bool,
}

// --- EXAMPLE: BUBBLE SORT ---
struct BubbleSort;
impl SortAlgorithm for BubbleSort {
    fn run_sort(
        &self,
        array: &mut Vec<i32>,
        sender: Sender<Frame>,
        stats: &mut Stats,
        delay_ms: Arc<AtomicU64>,
    ) {
        let n = array.len();
        for i in 0..n {
            for j in 0..n - i - 1 {
                stats.comparisons += 1;
                stats.active_idx = i as i32;
                stats.compare_idx = (j + 1) as i32;
                let _ = sender.send(Frame {
                    data: array.clone(),
                    stats: stats.clone(),
                });

                if array[j] > array[j + 1] {
                    array.swap(j, j + 1);
                    stats.swaps += 1;
                }
                // Artificial delay so we can see it
                thread::sleep(Duration::from_millis(delay_ms.load(Ordering::Relaxed)));
            }
        }
    }
}

// Data sent from Sort Thread -> UI Thread
#[derive(Clone)]
struct Frame {
    data: Vec<i32>,
    stats: Stats,
}

struct QuickSort;
impl QuickSort {
    fn partition(
        &self,
        array: &mut Vec<i32>,
        low: usize,
        high: usize,
        sender: &Sender<Frame>,
        stats: &mut Stats,
        delay_ms: &Arc<AtomicU64>,
    ) -> usize {
        let pivot_idx = high;
        let pivot_val = array[pivot_idx];
        let mut i = low;

        for j in low..high {
            stats.comparisons += 1;
            stats.active_idx = j as i32;
            stats.compare_idx = pivot_idx as i32;

            // Send frame to UI
            let _ = sender.send(Frame {
                data: array.clone(),
                stats: stats.clone(),
            });

            if array[j] < pivot_val {
                array.swap(i, j);
                stats.swaps += 1;
                i += 1;
            }
            thread::sleep(Duration::from_millis(delay_ms.load(Ordering::Relaxed)));
        }

        array.swap(i, high);
        stats.swaps += 1;
        i
    }

    fn quick_sort_recursive(
        &self,
        array: &mut Vec<i32>,
        low: usize,
        high: usize,
        sender: &Sender<Frame>,
        stats: &mut Stats,
        delay_ms: &Arc<AtomicU64>,
    ) {
        if low < high {
            let p = self.partition(array, low, high, sender, stats, delay_ms);

            // Handle potential underflow for usize in recursion
            if p > 0 {
                self.quick_sort_recursive(array, low, p - 1, sender, stats, delay_ms);
            }
            self.quick_sort_recursive(array, p + 1, high, sender, stats, delay_ms);
        }
    }
}

impl SortAlgorithm for QuickSort {
    fn run_sort(
        &self,
        array: &mut Vec<i32>,
        sender: Sender<Frame>,
        stats: &mut Stats,
        delay_ms: Arc<AtomicU64>,
    ) {
        let n = array.len();
        if n > 1 {
            self.quick_sort_recursive(array, 0, n - 1, &sender, stats, &delay_ms);
        }
    }
}

struct MergeSort;

impl MergeSort {
    fn merge(
        &self,
        array: &mut Vec<i32>,
        l: usize,
        m: usize,
        r: usize,
        sender: &Sender<Frame>,
        stats: &mut Stats,
        delay: &Arc<AtomicU64>,
    ) {
        let left_half = array[l..=m].to_vec();
        let right_half = array[m + 1..=r].to_vec();

        let (mut i, mut j, mut k) = (0, 0, l);

        while i < left_half.len() && j < right_half.len() {
            stats.comparisons += 1;
            stats.active_idx = k as i32;
            stats.compare_idx = (m + 1 + j) as i32;

            if left_half[i] <= right_half[j] {
                array[k] = left_half[i];
                i += 1;
            } else {
                array[k] = right_half[j];
                j += 1;
            }

            stats.swaps += 1; // In Merge Sort, we count writes as swaps/moves
            self.emit_frame(array, sender, stats, delay);
            k += 1;
        }

        while i < left_half.len() {
            array[k] = left_half[i];
            stats.active_idx = k as i32;
            self.emit_frame(array, sender, stats, delay);
            i += 1;
            k += 1;
        }

        while j < right_half.len() {
            array[k] = right_half[j];
            stats.active_idx = k as i32;
            self.emit_frame(array, sender, stats, delay);
            j += 1;
            k += 1;
        }
    }

    fn merge_sort_recursive(
        &self,
        array: &mut Vec<i32>,
        l: usize,
        r: usize,
        sender: &Sender<Frame>,
        stats: &mut Stats,
        delay: &Arc<AtomicU64>,
    ) {
        if l < r {
            let m = l + (r - l) / 2;
            self.merge_sort_recursive(array, l, m, sender, stats, delay);
            self.merge_sort_recursive(array, m + 1, r, sender, stats, delay);
            self.merge(array, l, m, r, sender, stats, delay);
        }
    }

    fn emit_frame(&self, array: &Vec<i32>, sender: &Sender<Frame>, stats: &Stats, delay: &Arc<AtomicU64>) {
        let _ = sender.send(Frame {
            data: array.clone(),
            stats: stats.clone(),
        });
        let current_delay = delay.load(Ordering::Relaxed);
        thread::sleep(Duration::from_millis(current_delay));
    }
}

impl SortAlgorithm for MergeSort {
    fn run_sort(&self, array: &mut Vec<i32>, sender: Sender<Frame>, stats: &mut Stats, delay: Arc<AtomicU64>) {
        let n = array.len();
        if n > 0 {
            self.merge_sort_recursive(array, 0, n - 1, &sender, stats, &delay);
        }
    }
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

    // Add this to your main function
    let delay_ms = Arc::new(AtomicU64::new(10)); // Default 10ms

    // Create the Channel
    let (tx, rx) = mpsc::channel();

    let mut current_frame = Frame {
        data: array.clone(),
        stats: Stats {
            active_idx: -1,
            compare_idx: -1,
            comparisons: 0,
            swaps: 0,
            is_done: true,
        },
    };

    loop {
        clear_background(BLACK);

        root_ui().window(
            hash!(),
            vec2(screen_width() - 200.0, 10.0),
            vec2(180.0, 120.0),
            |ui| {
                // --- Speed Slider ---
                let mut val = delay_ms.load(Ordering::Relaxed) as f32;
                ui.slider(hash!(), "Delay (ms)", 0.0..100.0, &mut val);
                delay_ms.store(val as u64, Ordering::Relaxed);

                ui.separator();

                if current_frame.stats.is_done {
                    if ui.button(None, "Bubble Sort") {
                        let tx_clone = tx.clone();
                        let delay_clone = Arc::clone(&delay_ms);
                        let mut sort_array = current_frame.data.clone();
                        thread::spawn(move || {
                            BubbleSort.sort(&mut sort_array, tx_clone, delay_clone)
                        });
                    }
                    if ui.button(None, "Quick Sort") {
                        let tx_clone = tx.clone();
                        let delay_clone = Arc::clone(&delay_ms);
                        let mut sort_array = current_frame.data.clone();
                        thread::spawn(move || {
                            QuickSort.sort(&mut sort_array, tx_clone, delay_clone)
                        });
                    }
                    if ui.button(None, "Merge Sort") {
                        let tx_clone = tx.clone();
                        let delay_clone = Arc::clone(&delay_ms);
                        let mut sort_array = current_frame.data.clone();
                        thread::spawn(move || {
                            MergeSort.sort(&mut sort_array, tx_clone, delay_clone);
                        });
                    }
                    if ui.button(None, "Shuffle") {
                        let mut rng = rand::rng();
                        current_frame.data.shuffle(&mut rng);
                    }
                } else {
                    ui.label(None, "Sorting...");
                }
            },
        );

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
