# Coordinated-Robot-Motion-Planning
The main goal of this project is to design and implement algorithmic solutions that compute good solutions of instances of the Coordinated Motion Planning problem proposed for the 2021 Geometry Challenge Edition: https://cgshop.ibr.cs.tu-bs.de/competition/cg-shop-2021/#problem-description

The problem known to be NP-complete, the objective was to compute good solutions to instances of this problem, based on two metrics : the makespan (the time until all agents have reached their destinations) and the total distance (the sum of distances taken by each agent).

We tackled the instances with a cooperative approach which prevents collisions between agents with shared knowledge of each other’s plans. The knowledge being positions reserved by robots at a certain timestamp. Randomized ordering and windowed search were one of the two main optimizations added to this approach.

