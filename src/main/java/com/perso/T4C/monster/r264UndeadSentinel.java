package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Undead Sentinel", x = 2115, y = 245, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2115, y = 345, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2215, y = 145, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2215, y = 445, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2315, y = 145, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2315, y = 445, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2415, y = 245, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2415, y = 345, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2510, y = 668, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2520, y = 613, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2520, y = 638, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2527, y = 687, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2529, y = 717, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2531, y = 561, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2534, y = 662, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2537, y = 627, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2541, y = 729, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2542, y = 599, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2542, y = 675, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2543, y = 702, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2548, y = 550, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2553, y = 572, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2561, y = 717, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2564, y = 751, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2568, y = 590, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2570, y = 517, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2573, y = 732, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2579, y = 782, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2582, y = 580, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2584, y = 559, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2587, y = 746, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2591, y = 526, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2594, y = 725, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2595, y = 773, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2598, y = 452, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2601, y = 493, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2602, y = 743, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2603, y = 552, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2604, y = 424, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2615, y = 785, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2619, y = 384, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2623, y = 802, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2626, y = 769, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2627, y = 505, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2629, y = 425, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2629, y = 534, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2630, y = 476, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2631, y = 407, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2633, y = 452, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2633, y = 820, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2638, y = 787, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2643, y = 299, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2644, y = 224, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2646, y = 501, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2647, y = 192, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2647, y = 375, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2649, y = 851, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2654, y = 449, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2655, y = 254, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2659, y = 326, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2661, y = 413, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2666, y = 370, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2666, y = 388, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2667, y = 176, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2669, y = 218, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2674, y = 287, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2677, y = 352, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2678, y = 810, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2678, y = 846, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2680, y = 231, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2680, y = 422, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2680, y = 443, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2681, y = 197, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2681, y = 251, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2681, y = 869, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2683, y = 160, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2689, y = 832, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2691, y = 263, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2691, y = 335, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2692, y = 239, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2694, y = 391, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2698, y = 296, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2698, y = 370, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2700, y = 414, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2702, y = 861, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2703, y = 450, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2704, y = 141, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2705, y = 172, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2706, y = 352, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2713, y = 427, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2715, y = 384, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2716, y = 229, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2717, y = 188, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2718, y = 899, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2719, y = 284, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2719, y = 337, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2723, y = 257, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2726, y = 856, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2727, y = 370, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2728, y = 121, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2729, y = 883, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2731, y = 153, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2734, y = 320, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2735, y = 269, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2736, y = 405, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2741, y = 344, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2743, y = 879, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2749, y = 105, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2753, y = 395, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2756, y = 130, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2757, y = 329, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2762, y = 355, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2763, y = 875, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2764, y = 923, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2769, y = 416, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2774, y = 102, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2776, y = 911, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2783, y = 130, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2785, y = 360, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2785, y = 894, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2791, y = 397, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2796, y = 154, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2796, y = 421, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2799, y = 117, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2806, y = 377, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2807, y = 354, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2807, y = 894, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2812, y = 837, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2813, y = 861, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2820, y = 904, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2821, y = 141, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2821, y = 426, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2826, y = 393, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2827, y = 443, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2829, y = 372, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2829, y = 822, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2830, y = 467, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2832, y = 296, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2835, y = 864, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2836, y = 155, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2836, y = 483, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2838, y = 187, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2839, y = 277, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2840, y = 906, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2846, y = 426, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2846, y = 453, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2848, y = 243, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2848, y = 832, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2849, y = 363, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2851, y = 137, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2854, y = 493, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2855, y = 797, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2856, y = 300, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2856, y = 403, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2857, y = 217, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2859, y = 200, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2861, y = 268, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2866, y = 510, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2866, y = 842, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2867, y = 164, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2867, y = 815, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2868, y = 377, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2872, y = 235, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2872, y = 315, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2872, y = 357, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2873, y = 460, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2875, y = 257, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2876, y = 755, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2877, y = 152, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2879, y = 432, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2879, y = 494, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2880, y = 285, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2881, y = 196, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2882, y = 412, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2883, y = 777, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2885, y = 531, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2886, y = 803, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2888, y = 176, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2890, y = 344, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2890, y = 553, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2891, y = 370, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2892, y = 454, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2893, y = 216, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2897, y = 262, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2897, y = 321, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2897, y = 507, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2901, y = 165, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2901, y = 404, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2901, y = 766, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2904, y = 183, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2904, y = 812, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2906, y = 424, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2906, y = 789, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2907, y = 242, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2907, y = 303, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2908, y = 275, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2908, y = 728, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2909, y = 483, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2909, y = 562, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2909, y = 748, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2912, y = 208, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2912, y = 224, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2912, y = 369, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2916, y = 517, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2916, y = 772, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2917, y = 384, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2918, y = 408, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2920, y = 447, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2920, y = 448, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2921, y = 337, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2923, y = 506, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2923, y = 581, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2926, y = 254, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2928, y = 789, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2928, y = 810, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2932, y = 403, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2932, y = 734, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2933, y = 525, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2935, y = 221, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2936, y = 357, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2937, y = 571, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2937, y = 600, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2938, y = 600, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2939, y = 380, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2940, y = 304, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2941, y = 692, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2942, y = 755, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2944, y = 244, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2948, y = 324, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2954, y = 569, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2955, y = 713, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2955, y = 733, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2957, y = 342, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2962, y = 593, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2966, y = 616, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2967, y = 701, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2968, y = 729, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2975, y = 649, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2977, y = 756, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2978, y = 673, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2983, y = 609, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2992, y = 657, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 2994, y = 737, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Sentinel", x = 3002, y = 686, z = 1, stationary = false, aggressive = true)
public final class r264UndeadSentinel extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r264UndeadSentinel(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Undead Sentinel",
        "${monster.undead_sentinel}",
        5641,
        0,
        10,
        30115,
        189,
        429,
        30000L,
        "64kSkeletonKing#m",
        "64kSkeletonKingA#k",
        "64kSkeletonKingC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        251,
        770,
        java.util.List.of(),
        false,
        0.0f,
        155,
        140,
        140,
        183,
        0,
        140,
        0,
        new int[] {47, 47, 63, 31, 5025, 47, 100, 100, 100, 100, 100, 100},
        140,
        570,
        0,
        1079083008,
        20057,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d241+188", 1690, 50, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10090, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
