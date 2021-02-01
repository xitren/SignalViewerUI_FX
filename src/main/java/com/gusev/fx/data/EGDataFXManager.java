package com.gusev.fx.data;

import com.gusev.data.DataContainer;
import com.gusev.data.DataModelJson;
import com.gusev.data.ExtendedDataLine;
import com.gusev.data.Mark;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

public class EGDataFXManager<T extends DataContainer> extends DataFXManager<T> {
    private String personName = "";
    private String personGender = "";
    private String personAge = "";
    private String personDetails = "";
    private String signalType = "";

    public EGDataFXManager(int n, ExtendedDataLine[] edl) {
        super(n, edl);
        sendGetUser();
    }

    public EGDataFXManager(int n) {
        super(n);
        sendGetUser();
    }

    private void sendGetUser() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            final String baseUrl = "http://localhost:12765/user";
            ResponseEntity<SecurityProperties.User> response = restTemplate.getForEntity(baseUrl, SecurityProperties.User.class);
            personName = response.getBody().getName();
            //personGender = response.getBody().getGender();
            //personAge = response.getBody().getAge();
        } catch (RuntimeException e) {
        }
    }

    public void saveToTXT(String filename) throws IOException {
        DataModelJson data = new DataModelJson(dataLines.size(), marks.size());
        Iterator<ExtendedDataLine<T>> it = dataLines.iterator();
        int i = 0;
        int length = 0;
        while (it.hasNext()) {
            ExtendedDataLine dlds = it.next();
            length = data.data[i].length;
            data.data[i++] = dlds.toArray();
        }
        data.data_label = getDataLabel();
        data.person_name = getPersonName();
        data.person_gender = getPersonGender();
        data.person_age = getPersonAge();
        data.person_details = getPersonDetails();
        data.person_date = getPersonDate();
        data.signal_type = getSignalType();
        Iterator<Mark> it2 = marks.iterator();
        i = 0;
        while (it2.hasNext()) {
            Mark dlds = it2.next();
            data.start[i] = dlds.start;
            data.finish[i] = dlds.finish;
            data.name[i] = dlds.name;
            data.channel[i] = dlds.channel;
            data.color[i] = dlds.color;
            data.label_color[i] = dlds.label_color;
            i++;
        }
        OutputStream outputStream = new FileOutputStream(filename);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write(String.format("%s\t<Ф.И.О.>\n", data.person_name));
        outputStreamWriter.write(String.format("%s\t<Пол.>\n", data.person_gender));
        outputStreamWriter.write(String.format("%s\t<Возраст, лет>\n", data.person_age));
        outputStreamWriter.write(String.format("%s\t<Примечания>\n", data.person_details));
        outputStreamWriter.write(String.format("%s\t<Дата проведения исследования: день, месяц, год>\n", data.person_date));
        outputStreamWriter.write(String.format("%d\t<Общее количество каналов>\n", dataLines.size()));
        outputStreamWriter.write(String.format("%d\t<Частота дискретизации, Гц>\n", data.discretization));
        outputStreamWriter.write(String.format("1\t<Количество групп физиологических сигналов>\n"));
        outputStreamWriter.write(String.format("%s\t<Тип сигналов 1-й группы>\n", data.signal_type));
        outputStreamWriter.write(String.format("%d\t<Количество сигналов 1-й группы>\n", dataLines.size()));
        outputStreamWriter.write(String.format("0,1250\t<Вес бита сигналов 1-й группы>\n"));
        outputStreamWriter.write(String.format("нВ\t<Единицы измерения сигналов 1-й группы>\n"));
        outputStreamWriter.write(String.format("\t<Идентификатор исследования>\n"));
        outputStreamWriter.write(String.format("1\t<Количество фрагментов записи>\n"));

        //Фрагмент
        outputStreamWriter.write(String.format("\t<Время начала 1-го фрагмента>\n", data.person_name));
        outputStreamWriter.write(String.format("%d\t<Продолжительность 1-го фрагмента в отсчетах, отсчетов>\n", length));
        for (int k = 0;k < data.data_label.length;k++) {
            outputStreamWriter.write(data.data_label[k] + "\t");
        }
        outputStreamWriter.write("\n");
        for (int ki = 0;ki < data.data.length;ki++) {
            for (int k = 0;k < data.data[ki].length;k++) {
                outputStreamWriter.write("" + data.data[ki][k] + "\t");
            }
            outputStreamWriter.write("\n");
        }
        outputStreamWriter.close();
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonGender() {
        return personGender;
    }

    public void setPersonGender(String personGender) {
        this.personGender = personGender;
    }

    public String getPersonAge() {
        return personAge;
    }

    public void setPersonAge(String personAge) {
        this.personAge = personAge;
    }

    public String getPersonDetails() {
        return personDetails;
    }

    public void setPersonDetails(String personDetails) {
        this.personDetails = personDetails;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }
}
