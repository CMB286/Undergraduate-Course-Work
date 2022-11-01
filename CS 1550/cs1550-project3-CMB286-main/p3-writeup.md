![Page Fault 16 frame, 4kb](https://github.com/cs1550-2221/cs1550-project3-CMB286/blob/801469fcfa5987c9b02fcf011a715fae18b15f32/Page%20Faults%20for%2016%20frames%20and%204kb.png)

![Disk Write 16 frame, 4kb](https://github.com/cs1550-2221/cs1550-project3-CMB286/blob/801469fcfa5987c9b02fcf011a715fae18b15f32/Disk%20Writes%20for%2016%20frames%20and%204kb.png)

![Page Fault 16 frame, 4096kb](https://github.com/cs1550-2221/cs1550-project3-CMB286/blob/801469fcfa5987c9b02fcf011a715fae18b15f32/Page%20Faults%20for%2016%20frames%20and%204096kb.png)

![Disk Write 16 frame, 4096kb](https://github.com/cs1550-2221/cs1550-project3-CMB286/blob/801469fcfa5987c9b02fcf011a715fae18b15f32/Disk%20Writes%20for%2016%20frames%20and%204096kb.png)

![Page Fault 1024 frame, 4096kb](https://github.com/cs1550-2221/cs1550-project3-CMB286/blob/801469fcfa5987c9b02fcf011a715fae18b15f32/Page%20Faults%20for%201024%20frames%20and%204096kb.png)

![Disk Write 1045 frame, 4096kb](https://github.com/cs1550-2221/cs1550-project3-CMB286/blob/801469fcfa5987c9b02fcf011a715fae18b15f32/Disk%20Writes%20for%201024%20frames%20and%204096kb.png)



By looking at the above graphs, we can see that the most best performance was achieved with 1024 frames and a page size of 4MB. This is because the only page faults we had were when initially putting in the addresses. The next best memory configuration was with 16 frames and a page size of 4MB. This input drastically reduced the total number of page faults and disk writes.