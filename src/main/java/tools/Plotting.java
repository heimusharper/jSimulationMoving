/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * Plotting is part of jSimulationMoving.
 *
 * jSimulationMoving is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSimulationMoving is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 */

package tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Класс, отвечающий за генерацию файлов для построения графиков
 * <p>
 * Created by boris on 08.02.17.
 */
public class Plotting {

    private ArrayList<FileUnit> generalListWOFire = new ArrayList<>();
    private ArrayList<FileUnit> generalListWFire  = new ArrayList<>();
    private ArrayList<FileUnit> transitionList    = new ArrayList<>();

    private String path = "/home/boris/workspace/java/jSimulationMoving/src/main/resources/";
    private String  workFile;
    private File    workDir;
    private boolean isFire;
    private ArrayList<Double> timeBlockZones = new ArrayList<>();

    public void addTimeBlock(double time) {
        if (isEndModelling) return;
        timeBlockZones.add(time);
    }

    public Plotting(String workFile, boolean isFire) {
        this.workFile = workFile;
        workDir = mkdirOut(workFile);
        this.isFire = isFire;
    }

    public void genGeneralPlotFile() {
        parseFileList();
        String plotData = "";
        String consts = "";
        int i = 1;
        if (isFire) {
            if (generalListWFire.isEmpty()) return;
            for (FileUnit u : generalListWFire) {
                plotData += createPlotString(u, i++);
                if (i % (6 + 1) == 0) i = 1;
            }
            plotData = plotTimeBlock(plotData, consts)[0];
            consts = plotTimeBlock(plotData, consts)[1];
        } else {
            if (generalListWOFire.isEmpty()) return;
            for (FileUnit u : generalListWOFire) {
                plotData += createPlotString(u, i++);
                if (i % (6 + 1) == 0) i = 1;
            }
        }

        String plotFile = "#!/usr/bin/gnuplot -persist \n\n"
                + "set terminal pngcairo size 1280,1024 #640,480\n"
                + "set output \"UdSU-GeneralDynamic-" + (isFire ? "Fire" : "NoFire") + ".png\"\n"
                + "set title \"Общая динамика эвакуации "+ (isFire ? "при пожаре" : "без пожара") + "\"\n"
                + "set xlabel \"Время, с\" font \"Liberation Serif,15\"\n"
                + "set ylabel \"Относительное количество людей, N/N_{общ}\" font \"Liberation Serif,15\"\n"
                + "set key left top samplen 5 spacing 1.15 width 0 box\n"
                + "set yrange [0:1.1]\n"
                + "set xtics 30\n"
                + "set ytics 0.1\n"
                + "set mxtics 10\n"
                + "set grid\n"
                + "set pointsize 1\n"
                + "set datafile separator \";\"\n"
                + "set style line 1 lt 1 pt 1\n"
                + "set style line 2 lt 2 pt 2\n"
                + "set style line 3 lt 3 pt 3\n"
                + "set style line 4 lt 4 pt 4\n"
                + "set style line 5 lt 5 pt 5\n"
                + "set style line 6 lt 6 pt 6\n\n"
                +"set parametric\n"
                +"set trange [0:1]\n\n"
                + consts
                + "plot \\\n"
                + plotData.substring(0, plotData.length()-4);

        try {
            gpFile =
                    getWorkDir().getAbsoluteFile() + "/" + getWorkDir().getName() + (isFire ? "-Fire" : "-NoFire") + "G"
                            + ".gp";
            PrintWriter writer = new PrintWriter(gpFile, "UTF-8");
            writer.print(plotFile);
            writer.close();
        } catch (IOException ignored) {}
    }

    private String gpFile;
    public String getGpFile() {
        return gpFile;
    }

    public void genTransitionPlotFile(ArrayList<String> doors) {
        parseFileList();
        if (transitionList.isEmpty()) return;
        String plotData = "";
        String consts = "";
        int i = 1;
        for (FileUnit u : transitionList) {
            if (!doors.isEmpty()) {
                if (doors.contains(u.getTypeN())) {
                    plotData += createPlotString(u, i++);
                    if (i % (7 + 1) == 0) i = 1;
                }
            } else {
                plotData += createPlotString(u, i++);
                if (i % (7 + 1) == 0) i = 1;
            }
        }

        plotData = plotTimeBlock(plotData, consts)[0];
        consts = plotTimeBlock(plotData, consts)[1];

        String plotFile = "#!/usr/bin/gnuplot -persist \n\n"
                + "set terminal pngcairo size 1280,1024 #640,480\n"
                + "set output \"UdSU-TransitionDynamic-" + (isFire ? "Fire" : "NoFire") + ".png\"\n"
                + "set title \"Динамика прохождения через проемы"+ (isFire ? "при пожаре" : "без пожара") + "\"\n"
                + "set xlabel \"Время, с\" font \"Liberation Serif,15\"\n"
                + "set ylabel \"Относительное количество людей, N/N_{общ}\" font \"Liberation Serif,15\"\n"
                + "set key left top samplen 5 spacing 1.15 width 0 box\n"
                + "set yrange [0:1.1]\n"
                + "set xtics 30\n"
                + "set ytics 0.1\n"
                + "set mxtics 10\n"
                + "set grid\n"
                + "set pointsize 1\n"
                + "set datafile separator \";\"\n"
                + "set style line 1 lt 1 pt 1\n"
                + "set style line 2 lt 2 pt 2\n"
                + "set style line 3 lt 3 pt 3\n"
                + "set style line 4 lt 4 pt 4\n"
                + "set style line 5 lt 5 pt 5\n"
                + "set style line 6 lt 6 pt 6\n"
                + "set style line 7 lt 7 pt 7\n\n"
                +"set parametric\n"
                +"set trange [0:1]\n\n"
                + consts
                + "plot \\\n"
                + plotData.substring(0, plotData.length()-4);

        try {
            gpFile = getWorkDir().getAbsoluteFile() + "/" + getWorkDir().getName() + "-T" + ".gp";
            PrintWriter writer = new PrintWriter(gpFile, "UTF-8");
            writer.print(plotFile);
            writer.close();
        } catch (IOException ignored) {}
    }

    private String[] plotTimeBlock(String plotData, String consts) {
        if (!timeBlockZones.isEmpty()) {
            int j = 0;
            for (double t : timeBlockZones) {
                consts += "const" + j + "=" + t + "\n";
                plotData += "const" + j + ",t title \"t_{блок} " + t + "\",\\\n";
                j++;
            }
        }
        return new String[]{plotData, consts};
    }

    private String createPlotString(FileUnit u, int i) {
        String s = (u.isGeneral()) ?
                " \"p" + u.getDensity() + " (" + u.getNumOfPeople() + ")" :
                " \"" + (u.isFireScenario() ? u.getTypeN() + " при пожаре" : u.getTypeN() + " без пожара");

        return "\"" + u.getName() + "\"" + " using 1:($2/" + u.getNumOfPeople() + ") smooth bezier title" + s + "\" "
                + "with " + "linespoints" + " linestyle" + " " + (i) + " ,\\\n";
    }

    /**
     * Фасовка файлов дирректории по типам
     */
    private void parseFileList() {
        for (String file : workDir.list()) {
            FileUnit unit = FileUnit.getSplitFileName(file);
            if (unit == null) continue;
            if (unit.isGeneral()) {
                if (unit.isFireScenario()) generalListWFire.add(unit);
                else generalListWOFire.add(unit);
            } else if (unit.isTransition()) transitionList.add(unit);
        }
    }

    /**
     * Создает рабочую дирректорию или возвращает, если уже создана
     *
     * @param workFile название файла сценария
     * @return Рабочую дирректорию
     */
    private File mkdirOut(String workFile) {
        File dirOut = new File(path + "out-" + getDate() + "-" + workFile.substring(0, workFile.length() - 4));
        if (!dirOut.exists()) dirOut.mkdir();
        return dirOut;
    }

    /**
     * @return Дата в формате "yyMMdd" для присединения к названию дирректории
     */
    private String getDate() {
        return new SimpleDateFormat("yyMMdd").format(new Date());
    }

    /**
     * @return Рабочая дирректория
     */
    public File getWorkDir() {
        return workDir;
    }

    private boolean isEndModelling = false;

    public void setEndModelling () {
        isEndModelling = !timeBlockZones.isEmpty();
    }

    private static class FileUnit {
        private String scenario;
        private Double density;
        private Double numOfPeople;
        private String type;
        private String typeN;
        private String name;

        private FileUnit(String scenario, Double density, Double numOfPeople, String type, String name, String typeN) {
            this.scenario = scenario;
            this.density = density;
            this.numOfPeople = numOfPeople;
            this.type = type;
            this.name = name;
            this.typeN = typeN;
        }

        private static FileUnit getSplitFileName(String fName) {
            String scenario = null;
            Double density = 0.0;
            Double numOfPeople = 0.0;
            String type = "";
            String typeN = "";

            if (!fName.contains("csv")) return null;
            String[] name = fName.split("-");
            if (name[1].equals("f")) scenario = "Fire";
            else if (name[1].equals("nf")) scenario = "NoFire";

            if (name[2].contains("d")) density = Double.parseDouble(name[2].substring(1));
            if (name[3].contains("p")) numOfPeople = Double.parseDouble(name[3].substring(1));
            if (name[4].contains("t")) {
                type = "Transition";
                typeN = name[4].substring(1, name[4].length()-4);
            }
            else if (name[4].contains("g")) type = "General";

            return new FileUnit(scenario, density, numOfPeople, type, fName, typeN);
        }

        private String getScenario() {
            return scenario;
        }

        private Double getDensity() {
            return density;
        }

        private Double getNumOfPeople() {
            return numOfPeople;
        }

        private String getType() {
            return type;
        }

        private boolean isFireScenario() {
            return getScenario().equals("Fire");
        }

        private boolean isGeneral() {
            return getType().equals("General");
        }

        private boolean isTransition() {
            return getType().equals("Transition");
        }

        private String getName() {
            return name;
        }

        private String getTypeN() {
            return typeN;
        }
    }
}
