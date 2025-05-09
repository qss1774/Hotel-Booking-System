import React, { useState, useEffect, useRef } from "react";
import ApiService from "../../service/ApiService";
import { DayPicker } from "react-day-picker";

const RoomSearch = ({ handSearchResult }) => {
    // State 状态解释（用来保存用户输入或系统数据）
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
//   roomType：当前选择的房型（例如 "Deluxe", "Standard"）

// roomTypes：所有可选房型的列表，会通过接口请求获取
  const [roomType, setRoomType] = useState("");
  const [roomTypes, setRoomTypes] = useState([]);
//   用来保存错误信息（比如用户没有选日期就点击了提交）
  const [error, setError] = useState("");

  //state for controlling calander visibility
//   控制两个日期选择器的显示/隐藏（true 显示，false 隐藏）
// | 代码部分                        | 含义                                    |
// | --------------------------- | ------------------------------------- |
// | `useState(false)`           | 初始化一个状态，初始值是 `false`，表示“起始日期的日历默认不显示” |
// | `isStartDatePickerVisible`  | 当前状态的值，布尔值，表示“起始日历是否显示”               |
// | `setStartDatePickerVisible` | 用于修改这个状态的函数（调用它可以改变显示或隐藏）             |

  const [isStartDatePickerVisible, setStartDatePickerVisible] = useState(false);
  const [isEndDatePickerVisible, setEndDatePickerVisible] = useState(false);

//   useRef 是 React 的一个 Hook，用来获取某个 HTML 元素的引用（比如 input）

// 后面可能会用来控制点击事件、聚焦、关闭等行为
  const startDateRef = useRef(null);
  const endDateRef = useRef(null);

//   useEffect 是 React 中用于副作用处理的钩子，在这里表示组件加载完毕时只执行一次

// [] 表示没有依赖，只有组件第一次加载时执行

  useEffect(() => {

    // 这是在 useEffect 作用域中定义的一个异步箭头函数，它的功能是：从后端获取房型列表，并设置到 state 中。
    const fetchRoomTypes = async () => {
      try {
        const types = await ApiService.getRoomsTypes();// 调用接口获取房型
        setRoomTypes(types);// 设置房型状态
      } catch (error) {
        console.log("Error fetching RoomTypes" + error);
      }
    };
    // 这是对上面那个函数的调用。调用后，它会去执行 ApiService.getRoomTypes() 请求，并将结果保存在 roomTypes 状态中。
    fetchRoomTypes();
  }, []);

//   当用户点击日历弹窗外部区域时，自动关闭日期选择器。
  const handleClickOutside = (event) => {
    // 这是一个事件处理函数，用来响应点击事件（mousedown）
    if (startDateRef.current && !startDateRef.current.contains(event.target)) {
        // 	确保这个元素存在（防止 null 报错）&& 如果点击的不是这个元素或它的子元素，返回 true
      setStartDatePickerVisible(false);
    //   获取 ref 引用的 DOM 元素（比如 <div ref={startDateRef}>）
    }
    if (endDateRef.current && !endDateRef.current.contains(event.target)) {
      setEndDatePickerVisible(false);
    }
  };

  useEffect(() => {
    // 页面挂载（组件渲染）后：添加一个事件监听器，监听整个 document 的 mousedown 事件
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
        // 页面卸载时（组件被销毁时）：清除监听器，防止内存泄露或多次绑定重复触发
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  //shoe error
//   显示一条错误信息，并在 timeout 毫秒后自动清除
  const showError = (message, timeout = 5000) => {
    setError(message);
    setTimeout(() => {
      setError("");
    }, timeout);
  };

  //this will fetch the rooms avialbale from our api
//   这是一个异步函数，点击“搜索”按钮时触发，功能是调用 API 获取符合条件的房间列表。
  const handleInternalSearch = async () => {
    // 输入验证阶段（Input Validation）
    if (!startDate || !endDate || !roomType) {
      showError("Please select fields");
      return false;
    }

    //  2. 格式化日期（转换为后端需要的格式）
    // startDate.toLocaleDateString("en-CA") 会把 JS 日期转换成 'YYYY-MM-DD' 格式，例如：2025-05-02
// en-CA 表示加拿大日期格式（即 ISO 标准）
// 这样可以传给后端识别使用
    try {
      const formattedStartDate = startDate
        ? startDate.toLocaleDateString("en-CA")
        : null;
      const formattedEndDate = endDate
        ? endDate.toLocaleDateString("en-CA")
        : null;

// 向 API 发起请求
// 调用 ApiService.getAvailableRooms() 方法，传入起始日期、结束日期、房型，使用 await 等待返回响应（这是个异步调用）
      const resp = await ApiService.getAvaliableRooms(
        formattedStartDate,
        formattedEndDate,
        roomType
      );
// 确保响应成功，状态码是 200。

      if (resp.status === 200) {
        //  情况一：无可用房间
        if (resp.rooms.length === 0) {
          showError("Room type not currently available for the selected date");
          return;
        }
        // 情况二：有可用房间
        handSearchResult(resp.rooms);
        // 搜索成功时清理掉旧的错误信息，保持页面整洁。
        setError("");
      }
    } catch (error) {
        // 优先显示后端返回的 error.response.data.message，否则就显示普通的 JS error.message
        // 安全写法，逐层判断，不会报错
      showError(error?.response?.data?.message || error.message);
    }
  };


return (
    <section>
      <div className="search-container">

        {/* | 动作    | 结果                                                        |
| ----- | --------------------------------------------------------- |
| 点击输入框 | `setStartDatePickerVisible(true)` → 显示日历                  |
| 选择某天  | `setStartDate(date)` + `setStartDatePickerVisible(false)` |
| 点击外部  | 在 `handleClickOutside()` 中识别 ref，关闭日历                     |
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
          {/* | 属性                                                        | 解释                          |
| --------------------------------------------------------- | --------------------------- |
| `type="text"`                                             | 文本框                         |
| `value={startDate ? startDate.toLocaleDateString() : ""}` | 如果用户已经选了日期，就显示格式化后的日期，否则显示空 |
| `placeholder="Select Check-In Date"`                      | 输入框中的提示文本                   |
| `onFocus={() => setStartDatePickerVisible(true)}`         | 当用户点击输入框时，显示日历              |
| `readOnly`                                                | 禁止手动输入（只能通过日历选择）            |
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
  
  {/* | 部分                               | 说明                              |
| -------------------------------- | ------------------------------- |
| `isStartDatePickerVisible`       | 控制这个弹窗是否显示                      |
| `ref={startDateRef}`             | 让点击外部时能识别这个区域（关闭日历）             |
| `DayPicker`                      | 第三方日期选择组件（可能是 react-day-picker） |
| `selected={startDate}`           | 当前选中的日期                         |
| `onDayClick={(date) => { ... }}` | 用户点击某个日期时执行                     |
| `month={startDate}`              | 显示的月份（可以是当前选中日期对应的月）            |
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
        {/* | 组件                                                    | 功能                                          |
| ----------------------------------------------------- | ------------------------------------------- |
| `<label>Room Type</label>`                            | 标签：“房型”                                     |
| `<select>`                                            | 下拉选择框，绑定当前房型变量 `roomType`                   |
| `value={roomType}`                                    | 当前选中的房型（受控组件 controlled component）          |
| `onChange={(e) => setRoomType(e.target.value)}`       | 当用户选择新的房型，就更新 `roomType` 状态                 |
| `<option disabled value="">Select Room Type</option>` | 默认提示选项，不能选                                  |
| `{roomTypes.map(...`                                  | 遍历 roomTypes 列表，动态生成选项项（例如 Deluxe, Suite 等） | */}

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
        {/* 按钮文字是 “Search Rooms”

点击后会触发 handleInternalSearch 函数（你前面写好的函数）

它会检查：是否选了开始时间、结束时间、房型

如果都有，就请求 API 获取符合条件的房间列表

如果缺少字段或房间不可用，就显示错误提示 */}
        <button className="home-search-button" onClick={handleInternalSearch}>
          Search Rooms
        </button>
      </div>
  
      {error && <p className="error-message">{error}</p>}
    </section>
  );
};


export default RoomSearch;
