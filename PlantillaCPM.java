import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

public class PlantillaCPM {
    FastReader reader = new FastReader();
    int n; // Número de tareas
    Task[] tasks;

    // Estructura para representar cada tarea del proyecto
    static class Task {
        int id;
        double optimista;
        double masProbable;
        double pesimista;
        double tiempoEsp;
        double varianza;

        List<Integer> predecessors = new ArrayList<>();
        List<Integer> successors = new ArrayList<>();

        double earlyStart = 0;
        double earlyFinish = 0;
        double lateStart = 0;
        double lateFinish = 0;
        double slack = 0;

        public Task(int id, double optimista, double masProbable, double pesimista) {
            this.id = id;
            this.optimista = optimista;
            this.masProbable = masProbable;
            this.pesimista = pesimista;

            // Cálculo del tiempo esperado (Distribución PERT)
            this.tiempoEsp = (optimista + 4.0 * masProbable + pesimista) / 6.0;

            // Cálculo de la varianza
            double term = (pesimista - optimista) / 6.0;
            this.varianza = term * term;
        }
    }

    static class FastReader {
        BufferedReader b;
        StringTokenizer s;

        public FastReader() {
            b = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() {
            while (s == null || !s.hasMoreElements()) {
                try {
                    s = new StringTokenizer(b.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return s.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }

        String nextLine() {
            String str = "";
            try {
                if (s.hasMoreTokens()) {
                    str = s.nextToken("\n");
                } else {
                    str = b.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }

    public void leerDatos() {
        n = reader.nextInt();
        tasks = new Task[n];

        // Leer tiempos PERT (optimista, más probable, pesimista) de cada tarea
        for (int i = 0; i < n; i++) {
            double o = reader.nextDouble();
            double m = reader.nextDouble();
            double p = reader.nextDouble();
            tasks[i] = new Task(i, o, m, p);
        }

        // Leer dependencias (predecesores para cada tarea)
        for (int i = 0; i < n; i++) {
            int numPredecessors = reader.nextInt();
            for (int j = 0; j < numPredecessors; j++) {
                int pred = reader.nextInt();
                tasks[i].predecessors.add(pred);
                tasks[pred].successors.add(i); // Construimos el grafo dirigido
            }
        }
    }

    //Cambioooooooooooo
    // Método para resolver el CPM y calcular los tiempos de inicio y fin tempranos y tardíos, así como la holgura

    public void resuelve() {
        // Aqui se pide el orden topológico de las tareas para asegurar que no haya ciclos en el grafo de dependencias
        List<Integer> topoOrder = obtenerOrdenTopologico();
        
        if (topoOrder.size() < n) {
            System.out.println("Error: El grafo contiene un ciclo. No se puede resolver CPM.");
            return;
        }

        // Forward Pass calculando Early Start y Early Finish
        double projectDuration = 0;

        for (int i : topoOrder) {
            Task t = tasks[i];
            double maxPredecessorEF = 0;
            for (int predId : t.predecessors) {
                maxPredecessorEF = Math.max(maxPredecessorEF, tasks[predId].earlyFinish);
            }
            t.earlyStart = maxPredecessorEF;
            t.earlyFinish = t.earlyStart + t.tiempoEsp;
            projectDuration = Math.max(projectDuration, t.earlyFinish);
        }

        // Backward Pass calculando Late Start y Late Finish
        for (int i = topoOrder.size() - 1; i >= 0; i--) {
            int taskId = topoOrder.get(i);
            Task t = tasks[taskId];
            
            if (t.successors.isEmpty()) {
                t.lateFinish = projectDuration;
            } else {
                double minSuccessorLS = Double.MAX_VALUE;
                for (int succId : t.successors) {
                    minSuccessorLS = Math.min(minSuccessorLS, tasks[succId].lateStart);
                }
                t.lateFinish = minSuccessorLS;
            }
            t.lateStart = t.lateFinish - t.tiempoEsp;
            t.slack = t.lateStart - t.earlyStart;
            
            if (Math.abs(t.slack) < 1e-7) {
                t.slack = 0;
            }
        }

        // Impresion de resultados
        // Esto alchile si conviene mas pedirle a una IA que te lo haga

        System.out.println("\n==================================================================");
        System.out.println("                   RESULTADOS DETALLADOS CPM/PERT                 ");
        System.out.println("==================================================================");
        System.out.printf("%-6s | %-8s | %-6s | %-6s | %-6s | %-6s | %-6s | %-8s\n", 
                          "Tarea", "Te(Esp)", "ES", "EF", "LS", "LF", "Holgura", "Critica?");
        System.out.println("------------------------------------------------------------------");

        List<Integer> criticalPath = new ArrayList<>();
        double projectVariance = 0;

        for (int i = 0; i < n; i++) {
            Task t = tasks[i];
            boolean isCritical = (t.slack == 0);
            if (isCritical) {
                criticalPath.add(i);
                projectVariance += t.varianza;
            }
            
            System.out.printf("T%-5d | %-8.2f | %-6.2f | %-6.2f | %-6.2f | %-6.2f | %-6.2f | %-8s\n",
                              t.id, t.tiempoEsp, t.earlyStart, t.earlyFinish, 
                              t.lateStart, t.lateFinish, t.slack, isCritical ? "Si" : "No");
        }
        System.out.println("==================================================================");
        System.out.printf("Duracion total esperada del proyecto: %.4f\n", projectDuration);
        System.out.print("Ruta Critica: ");
        for (int i = 0; i < criticalPath.size(); i++) {
            System.out.print("T" + criticalPath.get(i) + (i < criticalPath.size() - 1 ? " -> " : ""));
        }
        System.out.println();
        System.out.printf("Varianza total del proyecto: %.4f\n", projectVariance);
        System.out.printf("Desviacion estandar del proyecto: %.4f\n", Math.sqrt(projectVariance));
        System.out.println("==================================================================\n");
    }

    // Método para ordenar el grafo; Algoritmo de Kahn para orden topológico
    private List<Integer> obtenerOrdenTopologico() {
        int[] inDegree = new int[n];
        for (int i = 0; i < n; i++) {
            for (int succ : tasks[i].successors) {
                inDegree[succ]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (int v : tasks[u].successors) {
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                }
            }
        }
        return order;
    }

    public static void main(String[] args) {
        PlantillaCPM solver = new PlantillaCPM();
        solver.leerDatos();
        solver.resuelve();
    }
}