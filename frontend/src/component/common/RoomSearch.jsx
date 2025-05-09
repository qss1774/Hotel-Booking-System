import React, { useState, useEffect, useRef } from "react";
import ApiService from "../../service/ApiService";
import { DayPicker } from "react-day-picker";

const RoomSearch = ({ handSearchResult }) => {
    // State ״̬���ͣ����������û������ϵͳ���ݣ�
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
//   roomType����ǰѡ��ķ��ͣ����� "Deluxe", "Standard"��

// roomTypes�����п�ѡ���͵��б���ͨ���ӿ������ȡ
  const [roomType, setRoomType] = useState("");
  const [roomTypes, setRoomTypes] = useState([]);
//   �������������Ϣ�������û�û��ѡ���ھ͵�����ύ��
  const [error, setError] = useState("");

  //state for controlling calander visibility
//   ������������ѡ��������ʾ/���أ�true ��ʾ��false ���أ�
// | ���벿��                        | ����                                    |
// | --------------------------- | ------------------------------------- |
// | `useState(false)`           | ��ʼ��һ��״̬����ʼֵ�� `false`����ʾ����ʼ���ڵ�����Ĭ�ϲ���ʾ�� |
// | `isStartDatePickerVisible`  | ��ǰ״̬��ֵ������ֵ����ʾ����ʼ�����Ƿ���ʾ��               |
// | `setStartDatePickerVisible` | �����޸����״̬�ĺ��������������Ըı���ʾ�����أ�             |

  const [isStartDatePickerVisible, setStartDatePickerVisible] = useState(false);
  const [isEndDatePickerVisible, setEndDatePickerVisible] = useState(false);

//   useRef �� React ��һ�� Hook��������ȡĳ�� HTML Ԫ�ص����ã����� input��

// ������ܻ��������Ƶ���¼����۽����رյ���Ϊ
  const startDateRef = useRef(null);
  const endDateRef = useRef(null);

//   useEffect �� React �����ڸ����ô���Ĺ��ӣ��������ʾ����������ʱִֻ��һ��

// [] ��ʾû��������ֻ�������һ�μ���ʱִ��

  useEffect(() => {

    // ������ useEffect �������ж����һ���첽��ͷ���������Ĺ����ǣ��Ӻ�˻�ȡ�����б������õ� state �С�
    const fetchRoomTypes = async () => {
      try {
        const types = await ApiService.getRoomsTypes();// ���ýӿڻ�ȡ����
        setRoomTypes(types);// ���÷���״̬
      } catch (error) {
        console.log("Error fetching RoomTypes" + error);
      }
    };
    // ���Ƕ������Ǹ������ĵ��á����ú�����ȥִ�� ApiService.getRoomTypes() ���󣬲������������ roomTypes ״̬�С�
    fetchRoomTypes();
  }, []);

//   ���û�������������ⲿ����ʱ���Զ��ر�����ѡ������
  const handleClickOutside = (event) => {
    // ����һ���¼���������������Ӧ����¼���mousedown��
    if (startDateRef.current && !startDateRef.current.contains(event.target)) {
        // 	ȷ�����Ԫ�ش��ڣ���ֹ null ����&& �������Ĳ������Ԫ�ػ�������Ԫ�أ����� true
      setStartDatePickerVisible(false);
    //   ��ȡ ref ���õ� DOM Ԫ�أ����� <div ref={startDateRef}>��
    }
    if (endDateRef.current && !endDateRef.current.contains(event.target)) {
      setEndDatePickerVisible(false);
    }
  };

  useEffect(() => {
    // ҳ����أ������Ⱦ�������һ���¼����������������� document �� mousedown �¼�
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
        // ҳ��ж��ʱ�����������ʱ�����������������ֹ�ڴ�й¶���ΰ��ظ�����
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  //shoe error
//   ��ʾһ��������Ϣ������ timeout ������Զ����
  const showError = (message, timeout = 5000) => {
    setError(message);
    setTimeout(() => {
      setError("");
    }, timeout);
  };

  //this will fetch the rooms avialbale from our api
//   ����һ���첽�������������������ťʱ�����������ǵ��� API ��ȡ���������ķ����б�
  const handleInternalSearch = async () => {
    // ������֤�׶Σ�Input Validation��
    if (!startDate || !endDate || !roomType) {
      showError("Please select fields");
      return false;
    }

    //  2. ��ʽ�����ڣ�ת��Ϊ�����Ҫ�ĸ�ʽ��
    // startDate.toLocaleDateString("en-CA") ��� JS ����ת���� 'YYYY-MM-DD' ��ʽ�����磺2025-05-02
// en-CA ��ʾ���ô����ڸ�ʽ���� ISO ��׼��
// �������Դ������ʶ��ʹ��
    try {
      const formattedStartDate = startDate
        ? startDate.toLocaleDateString("en-CA")
        : null;
      const formattedEndDate = endDate
        ? endDate.toLocaleDateString("en-CA")
        : null;

// �� API ��������
// ���� ApiService.getAvailableRooms() ������������ʼ���ڡ��������ڡ����ͣ�ʹ�� await �ȴ�������Ӧ�����Ǹ��첽���ã�
      const resp = await ApiService.getAvaliableRooms(
        formattedStartDate,
        formattedEndDate,
        roomType
      );
// ȷ����Ӧ�ɹ���״̬���� 200��

      if (resp.status === 200) {
        //  ���һ���޿��÷���
        if (resp.rooms.length === 0) {
          showError("Room type not currently available for the selected date");
          return;
        }
        // ��������п��÷���
        handSearchResult(resp.rooms);
        // �����ɹ�ʱ������ɵĴ�����Ϣ������ҳ�����ࡣ
        setError("");
      }
    } catch (error) {
        // ������ʾ��˷��ص� error.response.data.message���������ʾ��ͨ�� JS error.message
        // ��ȫд��������жϣ����ᱨ��
      showError(error?.response?.data?.message || error.message);
    }
  };


return (
    <section>
      <div className="search-container">

        {/* | ����    | ���                                                        |
| ----- | --------------------------------------------------------- |
| �������� | `setStartDatePickerVisible(true)` �� ��ʾ����                  |
| ѡ��ĳ��  | `setStartDate(date)` + `setStartDatePickerVisible(false)` |
| ����ⲿ  | �� `handleClickOutside()` ��ʶ�� ref���ر�����                     |
 */}
  
          {/* checkj in date and calander field */}
        <div className="search-field" style={{ position: "relative" }}>
          <label>Check-in Date</label>
          <input
            type="text"
            value={startDate ? startDate.toLocaleDateString() : ""}
            placeholder="Select Check-In Date"
            onFocus={() => setStartDatePickerVisible(true)}
            readOnly
          />
          {/* | ����                                                        | ����                          |
| --------------------------------------------------------- | --------------------------- |
| `type="text"`                                             | �ı���                         |
| `value={startDate ? startDate.toLocaleDateString() : ""}` | ����û��Ѿ�ѡ�����ڣ�����ʾ��ʽ��������ڣ�������ʾ�� |
| `placeholder="Select Check-In Date"`                      | ������е���ʾ�ı�                   |
| `onFocus={() => setStartDatePickerVisible(true)}`         | ���û���������ʱ����ʾ����              |
| `readOnly`                                                | ��ֹ�ֶ����루ֻ��ͨ������ѡ��            |
 */}
  
          {isStartDatePickerVisible && (
            <div className="datepicker-container" ref={startDateRef}>
              <DayPicker
                selected={startDate}
                onDayClick={(date) => {
                  setStartDate(date);
                  setStartDatePickerVisible(false);
                }}
                month={startDate}
              />
            </div>
          )}
        </div>
  
  {/* | ����                               | ˵��                              |
| -------------------------------- | ------------------------------- |
| `isStartDatePickerVisible`       | ������������Ƿ���ʾ                      |
| `ref={startDateRef}`             | �õ���ⲿʱ��ʶ��������򣨹ر�������             |
| `DayPicker`                      | ����������ѡ������������� react-day-picker�� |
| `selected={startDate}`           | ��ǰѡ�е�����                         |
| `onDayClick={(date) => { ... }}` | �û����ĳ������ʱִ��                     |
| `month={startDate}`              | ��ʾ���·ݣ������ǵ�ǰѡ�����ڶ�Ӧ���£�            |
 */}
          
          {/* checkj out date and calander field */}
        <div className="search-field" style={{ position: "relative" }}>
          <label>Check-Out Date</label>
          <input
            type="text"
            value={endDate ? endDate.toLocaleDateString() : ""}
            placeholder="Select Check-Out Date"
            onFocus={() => setEndDatePickerVisible(true)}
            readOnly
          />
  
          {isEndDatePickerVisible && (
            <div className="datepicker-container" ref={endDateRef}>
              <DayPicker
                selected={endDate}
                onDayClick={(date) => {
                  setEndDate(date);
                  setEndDatePickerVisible(false);
                }}
                month={startDate}
              />
            </div>
          )}
        </div>
  
        {/* ROOM TYPE SELECTION FIELDS */}
        {/* | ���                                                    | ����                                          |
| ----------------------------------------------------- | ------------------------------------------- |
| `<label>Room Type</label>`                            | ��ǩ�������͡�                                     |
| `<select>`                                            | ����ѡ��򣬰󶨵�ǰ���ͱ��� `roomType`                   |
| `value={roomType}`                                    | ��ǰѡ�еķ��ͣ��ܿ���� controlled component��          |
| `onChange={(e) => setRoomType(e.target.value)}`       | ���û�ѡ���µķ��ͣ��͸��� `roomType` ״̬                 |
| `<option disabled value="">Select Room Type</option>` | Ĭ����ʾѡ�����ѡ                                  |
| `{roomTypes.map(...`                                  | ���� roomTypes �б���̬����ѡ������� Deluxe, Suite �ȣ� | */}

        <div className="search-field">
          <label>Room Type</label>
          <select value={roomType} onChange={(e)=> setRoomType(e.target.value)}>
              <option disabled value="">Select Room Type</option>
              {roomTypes.map((roomType) =>(
                  <option value={roomType} key={roomType}>
                      {roomType}
                  </option>
              ))}
          </select>
        </div>
  
        {/* SEARCH BUTTON */}
        {/* ��ť������ ��Search Rooms��

�����ᴥ�� handleInternalSearch ��������ǰ��д�õĺ�����

�����飺�Ƿ�ѡ�˿�ʼʱ�䡢����ʱ�䡢����

������У������� API ��ȡ���������ķ����б�

���ȱ���ֶλ򷿼䲻���ã�����ʾ������ʾ */}
        <button className="home-search-button" onClick={handleInternalSearch}>
          Search Rooms
        </button>
      </div>
  
      {error && <p className="error-message">{error}</p>}
    </section>
  );
};


export default RoomSearch;
