import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class PlantillaCPM {
    FastReader reader = new FastReader();
    int n; // Número de tareas
    Task[] tasks;

    // Estructura para representar cada tarea del proyecto
    static class Task {
        int id;
        int duration;

        // agregué o, m, p, tiempo esperado y varianza, cambie los datos s double por si
        // spawnea algún decimal
        double optimista;
        double masProbable;
        double pesimista;
        double tiempoEsp;
        double varianza;

        List<Integer> predecessors = new ArrayList<>();
        List<Integer> successors = new ArrayList<>();

        int earlyStart = 0;
        int earlyFinish = 0;
        int lateStart = 0;
        int lateFinish = 0;
        int slack = 0;

        public Task(int id, int duration, double optimista, double masProbable, double pesimista) {
            this.id = id;
            this.duration = duration;
            this.optimista = optimista;
            this.masProbable = masProbable;
            this.pesimista = pesimista;

            // Cálculo del tiempo esperado
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

        // leer la duración y tiempos de cada tarea
        for (int i = 0; i < n; i++) {
            int duration = reader.nextInt();
            double o = reader.nextDouble();
            double m = reader.nextDouble();
            double p = reader.nextDouble();
            tasks[i] = new Task(i, duration, o, m, p);
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

    public void resuelve() {
        // FORWARD PASS (Cálculo de Early Start y Early Finish)
        int projectDuration = 0;

        for (int i = 0; i < n; i++) {
            Task t = tasks[i];
            int maxPredecessorEF = 0;
            for (int predId : t.predecessors) {
                maxPredecessorEF = Math.max(maxPredecessorEF, tasks[predId].earlyFinish);
            }
            t.earlyStart = maxPredecessorEF;
            t.earlyFinish = t.earlyStart + t.duration;
            projectDuration = Math.max(projectDuration, t.earlyFinish);
        }

        // BACKWARD PASS (Cálculo de Late Finish y Late Start)
        for (int i = n - 1; i >= 0; i--) {
            Task t = tasks[i];
            if (t.successors.isEmpty()) {
                t.lateFinish = projectDuration;
            } else {
                int minSuccessorLS = Integer.MAX_VALUE;
                for (int succId : t.successors) {
                    minSuccessorLS = Math.min(minSuccessorLS, tasks[succId].lateStart);
                }
                t.lateFinish = minSuccessorLS;
            }
            t.lateStart = t.lateFinish - t.duration;
            t.slack = t.lateStart - t.earlyStart;
        }

        // RESULTADOS Y RUTA CRÍTICA
        System.out.println("Duración total del proyecto: " + projectDuration);
        System.out.print("Ruta Crítica (Tareas con holgura 0): ");

        List<Integer> criticalPath = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (tasks[i].slack == 0) {
                criticalPath.add(i);
            }
        }
        System.out.println(criticalPath);
    }

    public static void main(String[] args) {
        PlantillaCPM solver = new PlantillaCPM();
        solver.leerDatos();
        solver.resuelve();
    }
}